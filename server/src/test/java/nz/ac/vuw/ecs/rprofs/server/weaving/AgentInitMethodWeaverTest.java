package nz.ac.vuw.ecs.rprofs.server.weaving;

import java.util.List;

import com.google.common.collect.Lists;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.easymock.EasyMock.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 15/09/11
 */
public class AgentInitMethodWeaverTest {

	ClazzId id;
	Clazz clazz;
	ClassRecord record;
	Method method;
	ClassVisitor cvisitor;
	MethodVisitor visitor;

	@Before
	public void setUp() throws Exception {
		id = new ClazzId(1l);
		clazz = new Clazz(id, 0, "org/foo/Bar", null, null, 0, 0, false);
		record = new ClassRecord(clazz);
		record.generateMethod("_rprof_agent_init", "()V", ACC_STATIC);
		method = record.getMethod("_rprof_agent_init", "()V");

		cvisitor = createMock(ClassVisitor.class);
		visitor = createMock(MethodVisitor.class);
	}

	@Test
	public void testWeaveAgentInitCreateNoFields() {

		expect(cvisitor.visitMethod(ACC_STATIC, "_rprof_agent_init", "()V", null, null)).andReturn(visitor);
		visitor.visitCode();
		visitor.visitLdcInsn(Type.getType("Lorg/foo/Bar;"));
		visitor.visitInsn(ICONST_1); // class number
		visitor.visitInsn(ACONST_NULL); // no args, so null instead of array
		visitor.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "newcls", "(Ljava/lang/Object;I[I)V");
		visitor.visitInsn(RETURN);
		visitor.visitMaxs(3, 0);
		visitor.visitEnd();

		replay(cvisitor, visitor);

		AgentInitMethodWeaver.generate(record, cvisitor);

		verify(cvisitor, visitor);
	}

	@Test
	public void testWeaveAgentInitCreateWithFields() {

		List<Field> fields = Lists.newArrayList(
				new Field(new FieldId(id.getDatasetIndex(), id.getClassIndex(), (short) 4), 0, "foo",
						id, clazz.getName(), "I", ACC_PRIVATE),
				new Field(new FieldId(id.getDatasetIndex(), id.getClassIndex(), (short) 8), 0, "bar",
						id, clazz.getName(), "I", ACC_PUBLIC),
				new Field(new FieldId(id.getDatasetIndex(), id.getClassIndex(), (short) 12), 0, "baz",
						id, clazz.getName(), "I", ACC_PUBLIC | ACC_FINAL | ACC_STATIC)
		);
		record.addFields(fields);

		reset(cvisitor, visitor);

		expect(cvisitor.visitMethod(ACC_STATIC, "_rprof_agent_init", "()V", null, null)).andReturn(visitor);
		visitor.visitCode();
		visitor.visitLdcInsn(Type.getType("Lorg/foo/Bar;"));
		visitor.visitInsn(ICONST_1); // class number
		visitor.visitInsn(ICONST_2); // 3 fields, but we ignore static fields
		visitor.visitIntInsn(NEWARRAY, T_INT);
		visitor.visitInsn(DUP);
		visitor.visitInsn(ICONST_0); // position
		visitor.visitInsn(ICONST_4); // id
		visitor.visitInsn(IASTORE);
		visitor.visitInsn(DUP);
		visitor.visitInsn(ICONST_1); // position
		visitor.visitIntInsn(BIPUSH, 8); // id
		visitor.visitInsn(IASTORE);
		visitor.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "newcls", "(Ljava/lang/Object;I[I)V");
		visitor.visitInsn(RETURN);
		visitor.visitMaxs(6, 0);
		visitor.visitEnd();

		replay(cvisitor, visitor);

		AgentInitMethodWeaver.generate(record, cvisitor);

		verify(cvisitor, visitor);
	}
}
