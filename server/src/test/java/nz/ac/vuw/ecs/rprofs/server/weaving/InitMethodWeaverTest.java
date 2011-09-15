package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.easymock.EasyMock.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 15/09/11
 */
public class InitMethodWeaverTest {

	String name = "<init>";
	String desc = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";

	ClazzId id;
	Clazz clazz;
	ClassRecord record;
	Method method;
	MethodVisitor visitor;

	@Before
	public void setUp() throws Exception {
		id = new ClazzId(1l);
		clazz = new Clazz(id, "org/foo/Bar", null, null, 0);
		record = new ClassRecord(clazz);
		record.generateMethod(name, desc, ACC_STATIC);
		method = record.getMethod(name, desc);

		visitor = createMock(MethodVisitor.class);
	}

	@Test
	public void test() throws Exception {

		resetToStrict(visitor);

		visitor.visitCode();
		visitor.visitLabel(anyObject(Label.class));
		visitor.visitInsn(ICONST_1);
		visitor.visitInsn(ICONST_0);
		visitor.visitInsn(ICONST_4);
		visitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
		visitor.visitInsn(DUP);
		visitor.visitInsn(ICONST_0);
		visitor.visitVarInsn(ALOAD, 1);
		visitor.visitInsn(AASTORE);
		visitor.visitInsn(DUP);
		visitor.visitInsn(ICONST_1);
		visitor.visitVarInsn(ALOAD, 2);
		visitor.visitInsn(AASTORE);
		visitor.visitInsn(DUP);
		visitor.visitInsn(ICONST_2);
		visitor.visitVarInsn(ALOAD, 3);
		visitor.visitInsn(AASTORE);
		visitor.visitInsn(DUP);
		visitor.visitInsn(ICONST_3);
		visitor.visitVarInsn(ALOAD, 4);
		visitor.visitInsn(AASTORE);
		visitor.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "enter", "(II[Ljava/lang/Object;)V");

		visitor.visitVarInsn(ALOAD, 0);
		visitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");

		visitor.visitInsn(ICONST_1);
		visitor.visitInsn(ICONST_0);
		visitor.visitVarInsn(ALOAD, 0);
		visitor.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "exit", "(IILjava/lang/Object;)V");
		visitor.visitInsn(RETURN);

		visitor.visitLabel(anyObject(Label.class));
		visitor.visitTryCatchBlock(anyObject(Label.class), anyObject(Label.class), anyObject(Label.class),
				eq("java/lang/Exception"));
		visitor.visitLabel(anyObject(Label.class));

		visitor.visitFrame(eq(0), eq(0), EasyMock.<Object[]>anyObject(), eq(1), EasyMock.<Object[]>anyObject());

		visitor.visitVarInsn(ASTORE, 1);
		visitor.visitInsn(ICONST_1);
		visitor.visitInsn(ICONST_0);
		visitor.visitVarInsn(ALOAD, 1);
		visitor.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "except", "(IILjava/lang/Object;)V");
		visitor.visitVarInsn(ALOAD, 1);
		visitor.visitInsn(ATHROW);

		visitor.visitMaxs(6, 5);
		visitor.visitEnd();

		replay(visitor);

		MethodVisitor mv = new InitMethodWeaver(record, method, visitor);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
		mv.visitInsn(RETURN);
		mv.visitMaxs(1, 5);
		mv.visitEnd();


		verify(visitor);
	}
}
