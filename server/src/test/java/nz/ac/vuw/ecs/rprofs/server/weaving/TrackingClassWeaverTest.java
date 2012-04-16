package nz.ac.vuw.ecs.rprofs.server.weaving;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Scanner;

import com.google.common.collect.Lists;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.*;

import static org.junit.Assert.assertEquals;
import static org.objectweb.asm.Opcodes.*;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 15/09/11
 */
public class TrackingClassWeaverTest extends WeaverTestBase {

	private ClazzId id;
	private Clazz clazz;
	private String name;
	private ClazzId parent;
	private String parentName;
	private ClassRecord record;

	@Before
	public void setUp() throws Exception {
		id = new ClazzId(1l);
		name = "nz/ac/vuw/ecs/rprof/HeapTracker";
		parent = new ClazzId(2l);
		parentName = "java/lang/Object";
		clazz = new Clazz(id, 0, name, parent, parentName, 0, 0, false);
		record = new ClassRecord(clazz);
		record.addFields(Lists.<Field>newArrayList(
				new Field(new FieldId((short) 0, 1, (short) 1), 0, "engaged", id, name,
						"I", ACC_PRIVATE | ACC_STATIC),
				new Field(new FieldId((short) 0, 1, (short) 2), 0, "cnum", id, name,
						"I", ACC_PRIVATE | ACC_STATIC),
				new Field(new FieldId((short) 0, 1, (short) 3), 0, "nullCounter", id, name,
						"Lnz/ac/vuw/ecs/rprof/HeapTracker;", ACC_PRIVATE | ACC_STATIC),
				new Field(new FieldId((short) 0, 1, (short) 4), 0, "nextThreadId", id, name,
						"S", ACC_PRIVATE | ACC_STATIC | ACC_VOLATILE),
				new Field(new FieldId((short) 0, 1, (short) 5), 0, "thread", id, name,
						"J", ACC_PRIVATE | ACC_FINAL),
				new Field(new FieldId((short) 0, 1, (short) 6), 0, "counter", id, name,
						"J", ACC_PRIVATE)
		));
		record.addMethods(Lists.<Method>newArrayList(
				new Method(new MethodId((short) 0, 1, (short) 1), 0, "create", id, name,
						"()Lnz/ac/vuw/ecs/rprof/HeapTracker;", ACC_PUBLIC | ACC_STATIC),
				new Method(new MethodId((short) 0, 1, (short) 2), 0, "_newcls", id, name,
						"(Ljava/lang/Object;I[I)V", ACC_PRIVATE | ACC_STATIC | ACC_NATIVE),
				new Method(new MethodId((short) 0, 1, (short) 3), 0, "newcls", id, name,
						"(Ljava/lang/Object;I[I)V", ACC_PUBLIC | ACC_STATIC),
				new Method(new MethodId((short) 0, 1, (short) 4), 0, "_newobj", id, name,
						"(Ljava/lang/Object;Ljava/lang/Object;J)V", ACC_PRIVATE | ACC_STATIC | ACC_NATIVE),
				new Method(new MethodId((short) 0, 1, (short) 5), 0, "newobj", id, name,
						"(Ljava/lang/Object;)V", ACC_PUBLIC | ACC_STATIC),
				new Method(new MethodId((short) 0, 1, (short) 6), 0, "_newarr", id, name,
						"(Ljava/lang/Object;Ljava/lang/Object;J)V", ACC_PRIVATE | ACC_STATIC | ACC_NATIVE),
				new Method(new MethodId((short) 0, 1, (short) 7), 0, "newarr", id, name,
						"(Ljava/lang/Object;)V", ACC_PUBLIC | ACC_STATIC),
				new Method(new MethodId((short) 0, 1, (short) 8), 0, "_menter", id, name,
						"(Ljava/lang/Object;II[Ljava/lang/Object;)V", ACC_PRIVATE | ACC_STATIC | ACC_NATIVE),
				new Method(new MethodId((short) 0, 1, (short) 9), 0, "enter", id, name,
						"(II[Ljava/lang/Object;)V", ACC_PUBLIC | ACC_STATIC),
				new Method(new MethodId((short) 0, 1, (short) 10), 0, "_mexit", id, name,
						"(Ljava/lang/Object;IILjava/lang/Object;)V", ACC_PRIVATE | ACC_STATIC | ACC_NATIVE),
				new Method(new MethodId((short) 0, 1, (short) 11), 0, "exit", id, name,
						"(IILjava/lang/Object;)V", ACC_PUBLIC | ACC_STATIC),
				new Method(new MethodId((short) 0, 1, (short) 12), 0, "_mexcept", id, name,
						"(Ljava/lang/Object;IILjava/lang/Object;)V", ACC_PRIVATE | ACC_STATIC | ACC_NATIVE),
				new Method(new MethodId((short) 0, 1, (short) 13), 0, "except", id, name,
						"(IILjava/lang/Object;)V", ACC_PUBLIC | ACC_STATIC),
				new Method(new MethodId((short) 0, 1, (short) 14), 0, "_main", id, name,
						"(Ljava/lang/Object;II)V", ACC_PRIVATE | ACC_STATIC | ACC_NATIVE),
				new Method(new MethodId((short) 0, 1, (short) 15), 0, "main", id, name,
						"(II)V", ACC_PUBLIC | ACC_STATIC),

				new Method(new MethodId((short) 0, 1, (short) 16), 0, "id", id, name,
						"(Ljava/lang/Object;)J", ACC_PRIVATE | ACC_STATIC),
				new Method(new MethodId((short) 0, 1, (short) 17), 0, "getTracker", id, name,
						"()Lnz/ac/vuw/ecs/rprof/HeapTracker;", ACC_PRIVATE | ACC_STATIC),
				new Method(new MethodId((short) 0, 1, (short) 18), 0, "_getTracker", id, name,
						"(Ljava/lang/Thread;)Lnz/ac/vuw/ecs/rprof/HeapTracker;", ACC_PUBLIC | ACC_STATIC),
				new Method(new MethodId((short) 0, 1, (short) 19), 0, "_setTracker", id, name,
						"(Ljava/lang/Thread;Lnz/ac/vuw/ecs/rprof/HeapTracker;)V", ACC_PUBLIC | ACC_STATIC),

				new Method(new MethodId((short) 0, 1, (short) 20), 0, "nextThreadId", id, name,
						"()S", ACC_PRIVATE | ACC_STATIC | ACC_SYNCHRONIZED),

				new Method(new MethodId((short) 0, 1, (short) 21), 0, "<init>", id, name,
						"()V", ACC_PRIVATE),
				new Method(new MethodId((short) 0, 1, (short) 22), 0, "<init>", id, name,
						"(S)V", ACC_PRIVATE),
				new Method(new MethodId((short) 0, 1, (short) 23), 0, "newId", id, name,
						"()J", ACC_PUBLIC),
				new Method(new MethodId((short) 0, 1, (short) 24), 0, "<clinit>", id, name,
						"()V", ACC_STATIC)
		));
	}

	@Test
	public void testTrackerWeavingAsLines() throws Exception {
		byte[] input = generateTrackerClass();
		byte[] expect = generateWovenTrackerClass(record);

		byte[] output = new Weaver().weave(record, input);

		ByteArrayOutputStream expectedOutput = new ByteArrayOutputStream();
		print(expect, expectedOutput);

		ByteArrayOutputStream actualOutput = new ByteArrayOutputStream();
		print(output, actualOutput);

		Scanner e = new Scanner(new ByteArrayInputStream(expectedOutput.toByteArray()));
		Scanner a = new Scanner(new ByteArrayInputStream(actualOutput.toByteArray()));

		int count = 0;
		while (e.hasNextLine() && a.hasNextLine()) {
			assertEquals("Lines " + count + " differ:", e.nextLine(), a.nextLine());
			count++;
		}
		assertEquals(e.hasNextLine(), a.hasNextLine());
	}

	@Test
	public void testTrackerWeavingAsStream() throws Exception {
		byte[] input = generateTrackerClass();
		byte[] expect = generateWovenTrackerClass(record);

		byte[] output = new Weaver().weave(record, input);

		ByteArrayOutputStream expectedOutput = new ByteArrayOutputStream();
		print(expect, expectedOutput);

		ByteArrayOutputStream actualOutput = new ByteArrayOutputStream();
		print(output, actualOutput);

		Scanner e = new Scanner(new ByteArrayInputStream(expectedOutput.toByteArray()));
		Scanner a = new Scanner(new ByteArrayInputStream(actualOutput.toByteArray()));

		assertEquals(expectedOutput.toString(), actualOutput.toString());
	}

	byte[] generateTrackerClass() {
		ClassWriter cw = new ClassWriter(0);

		FieldVisitor fv;
		MethodVisitor mv;
		AnnotationVisitor av0;

		cw.visit(51, ACC_PUBLIC + ACC_SUPER, "nz/ac/vuw/ecs/rprof/HeapTracker", null, "java/lang/Object", null);

		{
			fv = cw.visitField(ACC_PRIVATE + ACC_STATIC, "engaged", "I", null, null);
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE + ACC_STATIC, "cnum", "I", null, null);
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE + ACC_STATIC, "nullCounter", "Lnz/ac/vuw/ecs/rprof/HeapTracker;", null, null);
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE + ACC_STATIC + ACC_VOLATILE, "nextThreadId", "S", null, null);
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "thread", "J", null, null);
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE, "counter", "J", null, null);
			fv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "create", "()Lnz/ac/vuw/ecs/rprof/HeapTracker;", null, null);
			mv.visitCode();
			mv.visitTypeInsn(NEW, "nz/ac/vuw/ecs/rprof/HeapTracker");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "nz/ac/vuw/ecs/rprof/HeapTracker", "<init>", "()V");
			mv.visitInsn(ARETURN);
			mv.visitMaxs(2, 0);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC + ACC_NATIVE, "_newcls", "(Ljava/lang/Object;I[I)V", null, null);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "newcls", "(Ljava/lang/Object;I[I)V", null, null);
			mv.visitCode();
			mv.visitFieldInsn(GETSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "engaged", "I");
			Label l0 = new Label();
			mv.visitJumpInsn(IFEQ, l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "_newcls", "(Ljava/lang/Object;I[I)V");
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(RETURN);
			mv.visitMaxs(3, 3);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC + ACC_NATIVE, "_newobj", "(Ljava/lang/Object;Ljava/lang/Object;J)V", null, null);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "newobj", "(Ljava/lang/Object;)V", null, null);
			mv.visitCode();
			mv.visitFieldInsn(GETSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "engaged", "I");
			Label l0 = new Label();
			mv.visitJumpInsn(IFEQ, l0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "id", "(Ljava/lang/Object;)J");
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "_newobj", "(Ljava/lang/Object;Ljava/lang/Object;J)V");
			mv.visitLabel(l0);
			mv.visitFrame(F_SAME, 0, null, 0, null);
			mv.visitInsn(RETURN);
			mv.visitMaxs(4, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC + ACC_NATIVE, "_newarr", "(Ljava/lang/Object;Ljava/lang/Object;J)V", null, null);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "newarr", "(Ljava/lang/Object;)V", null, null);
			mv.visitCode();
			mv.visitFieldInsn(GETSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "engaged", "I");
			Label l0 = new Label();
			mv.visitJumpInsn(IFEQ, l0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "id", "(Ljava/lang/Object;)J");
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "_newarr", "(Ljava/lang/Object;Ljava/lang/Object;J)V");
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(RETURN);
			mv.visitMaxs(4, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC + ACC_NATIVE, "_menter", "(Ljava/lang/Object;II[Ljava/lang/Object;)V", null, null);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "enter", "(II[Ljava/lang/Object;)V", null, null);
			mv.visitCode();
			mv.visitFieldInsn(GETSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "engaged", "I");
			Label l0 = new Label();
			mv.visitJumpInsn(IFEQ, l0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;");
			mv.visitVarInsn(ILOAD, 0);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "_menter", "(Ljava/lang/Object;II[Ljava/lang/Object;)V");
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(RETURN);
			mv.visitMaxs(4, 3);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC + ACC_NATIVE, "_mexit", "(Ljava/lang/Object;IILjava/lang/Object;)V", null, null);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "exit", "(IILjava/lang/Object;)V", null, null);
			mv.visitCode();
			mv.visitFieldInsn(GETSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "engaged", "I");
			Label l0 = new Label();
			mv.visitJumpInsn(IFEQ, l0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;");
			mv.visitVarInsn(ILOAD, 0);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "_mexit", "(Ljava/lang/Object;IILjava/lang/Object;)V");
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(RETURN);
			mv.visitMaxs(4, 3);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC + ACC_NATIVE, "_mexcept", "(Ljava/lang/Object;IILjava/lang/Object;)V", null, null);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "except", "(IILjava/lang/Object;)V", null, null);
			mv.visitCode();
			mv.visitFieldInsn(GETSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "engaged", "I");
			Label l0 = new Label();
			mv.visitJumpInsn(IFEQ, l0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;");
			mv.visitVarInsn(ILOAD, 0);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "_mexcept", "(Ljava/lang/Object;IILjava/lang/Object;)V");
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(RETURN);
			mv.visitMaxs(4, 3);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC + ACC_NATIVE, "_main", "(Ljava/lang/Object;II)V", null, null);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "(II)V", null, null);
			mv.visitCode();
			mv.visitFieldInsn(GETSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "engaged", "I");
			Label l0 = new Label();
			mv.visitJumpInsn(IFEQ, l0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;");
			mv.visitVarInsn(ILOAD, 0);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "_main", "(Ljava/lang/Object;II)V");
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(RETURN);
			mv.visitMaxs(3, 2);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC, "id", "(Ljava/lang/Object;)J", null, null);
			mv.visitCode();
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "getTracker", "()Lnz/ac/vuw/ecs/rprof/HeapTracker;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "nz/ac/vuw/ecs/rprof/HeapTracker", "newId", "()J");
			mv.visitInsn(LRETURN);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC, "getTracker", "()Lnz/ac/vuw/ecs/rprof/HeapTracker;", null, null);
			mv.visitCode();
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE, 0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;");
			mv.visitVarInsn(ASTORE, 1);
			mv.visitVarInsn(ALOAD, 1);
			Label l0 = new Label();
			mv.visitJumpInsn(IFNULL, l0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "_getTracker", "(Ljava/lang/Thread;)Lnz/ac/vuw/ecs/rprof/HeapTracker;");
			mv.visitVarInsn(ASTORE, 0);
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_APPEND, 2, new Object[]{"nz/ac/vuw/ecs/rprof/HeapTracker", "java/lang/Thread"}, 0, null);
			mv.visitVarInsn(ALOAD, 0);
			Label l1 = new Label();
			mv.visitJumpInsn(IFNONNULL, l1);
			mv.visitFieldInsn(GETSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "nullCounter", "Lnz/ac/vuw/ecs/rprof/HeapTracker;");
			mv.visitVarInsn(ASTORE, 0);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(1, 2);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "_getTracker", "(Ljava/lang/Thread;)Lnz/ac/vuw/ecs/rprof/HeapTracker;", null, null);
			mv.visitCode();
			mv.visitInsn(ACONST_NULL);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "_setTracker", "(Ljava/lang/Thread;Lnz/ac/vuw/ecs/rprof/HeapTracker;)V", null, null);
			mv.visitCode();
			mv.visitInsn(RETURN);
			mv.visitMaxs(0, 2);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC + ACC_SYNCHRONIZED, "nextThreadId", "()S", null, null);
			mv.visitCode();
			mv.visitFieldInsn(GETSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "nextThreadId", "S");
			mv.visitVarInsn(ISTORE, 0);
			mv.visitVarInsn(ILOAD, 0);
			mv.visitVarInsn(ILOAD, 0);
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);
			mv.visitInsn(I2S);
			mv.visitVarInsn(ISTORE, 0);
			mv.visitFieldInsn(PUTSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "nextThreadId", "S");
			mv.visitVarInsn(ILOAD, 0);
			mv.visitInsn(IRETURN);
			mv.visitMaxs(3, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "nextThreadId", "()S");
			mv.visitMethodInsn(INVOKESPECIAL, "nz/ac/vuw/ecs/rprof/HeapTracker", "<init>", "(S)V");
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE, "<init>", "(S)V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(LCONST_0);
			mv.visitFieldInsn(PUTFIELD, "nz/ac/vuw/ecs/rprof/HeapTracker", "counter", "J");
			mv.visitLdcInsn(Type.getType("Lnz/ac/vuw/ecs/rprof/HeapTracker;"));
			mv.visitFieldInsn(GETSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "cnum", "I");
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "newcls", "(Ljava/lang/Object;I[I)V");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitInsn(I2L);
			mv.visitIntInsn(BIPUSH, 32);
			mv.visitInsn(LSHL);
			mv.visitFieldInsn(PUTFIELD, "nz/ac/vuw/ecs/rprof/HeapTracker", "thread", "J");
			mv.visitInsn(RETURN);
			mv.visitMaxs(4, 2);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "newId", "()J", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "nz/ac/vuw/ecs/rprof/HeapTracker", "thread", "J");
			mv.visitInsn(LCONST_0);
			mv.visitInsn(LCMP);
			Label l0 = new Label();
			mv.visitJumpInsn(IFNE, l0);
			mv.visitInsn(LCONST_0);
			Label l1 = new Label();
			mv.visitJumpInsn(GOTO, l1);
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "nz/ac/vuw/ecs/rprof/HeapTracker", "thread", "J");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(DUP);
			mv.visitFieldInsn(GETFIELD, "nz/ac/vuw/ecs/rprof/HeapTracker", "counter", "J");
			mv.visitInsn(LCONST_1);
			mv.visitInsn(LADD);
			mv.visitInsn(DUP2_X1);
			mv.visitFieldInsn(PUTFIELD, "nz/ac/vuw/ecs/rprof/HeapTracker", "counter", "J");
			mv.visitInsn(LOR);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{Opcodes.LONG});
			mv.visitInsn(LRETURN);
			mv.visitMaxs(7, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
			mv.visitCode();
			mv.visitInsn(ICONST_0);
			mv.visitFieldInsn(PUTSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "engaged", "I");
			mv.visitInsn(ICONST_1);
			mv.visitFieldInsn(PUTSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "nextThreadId", "S");
			mv.visitTypeInsn(NEW, "nz/ac/vuw/ecs/rprof/HeapTracker");
			mv.visitInsn(DUP);
			mv.visitInsn(ICONST_0);
			mv.visitMethodInsn(INVOKESPECIAL, "nz/ac/vuw/ecs/rprof/HeapTracker", "<init>", "(S)V");
			mv.visitFieldInsn(PUTSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "nullCounter", "Lnz/ac/vuw/ecs/rprof/HeapTracker;");
			mv.visitInsn(RETURN);
			mv.visitMaxs(3, 0);
			mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}

	byte[] generateWovenTrackerClass(ClassRecord record) {
		ClassWriter cw = new ClassWriter(0);

		FieldVisitor fv;
		MethodVisitor mv;
		AnnotationVisitor av0;

		cw.visit(51, ACC_PUBLIC + ACC_SUPER, "nz/ac/vuw/ecs/rprof/HeapTracker", null, "java/lang/Object", null);

		{
			fv = cw.visitField(ACC_PRIVATE + ACC_STATIC, "engaged", "I", null, null);
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE + ACC_STATIC, "cnum", "I", null, record.getId().getClassIndex());
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE + ACC_STATIC, "nullCounter", "Lnz/ac/vuw/ecs/rprof/HeapTracker;", null, null);
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE + ACC_STATIC + ACC_VOLATILE, "nextThreadId", "S", null, null);
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "thread", "J", null, null);
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE, "counter", "J", null, null);
			fv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "create", "()Lnz/ac/vuw/ecs/rprof/HeapTracker;", null, null);
			mv.visitCode();
			mv.visitTypeInsn(NEW, "nz/ac/vuw/ecs/rprof/HeapTracker");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "nz/ac/vuw/ecs/rprof/HeapTracker", "<init>", "()V");
			mv.visitInsn(ARETURN);
			mv.visitMaxs(2, 0);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC + ACC_NATIVE, "_newcls", "(Ljava/lang/Object;I[I)V", null, null);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "newcls", "(Ljava/lang/Object;I[I)V", null, null);
			mv.visitCode();
			mv.visitFieldInsn(GETSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "engaged", "I");
			Label l0 = new Label();
			mv.visitJumpInsn(IFEQ, l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "_newcls", "(Ljava/lang/Object;I[I)V");
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(RETURN);
			mv.visitMaxs(3, 3);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC + ACC_NATIVE, "_newobj", "(Ljava/lang/Object;Ljava/lang/Object;J)V", null, null);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "newobj", "(Ljava/lang/Object;)V", null, null);
			mv.visitCode();
			mv.visitFieldInsn(GETSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "engaged", "I");
			Label l0 = new Label();
			mv.visitJumpInsn(IFEQ, l0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "id", "(Ljava/lang/Object;)J");
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "_newobj", "(Ljava/lang/Object;Ljava/lang/Object;J)V");
			mv.visitLabel(l0);
			mv.visitFrame(F_SAME, 0, null, 0, null);
			mv.visitInsn(RETURN);
			mv.visitMaxs(4, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC + ACC_NATIVE, "_newarr", "(Ljava/lang/Object;Ljava/lang/Object;J)V", null, null);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "newarr", "(Ljava/lang/Object;)V", null, null);
			mv.visitCode();
			mv.visitFieldInsn(GETSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "engaged", "I");
			Label l0 = new Label();
			mv.visitJumpInsn(IFEQ, l0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "id", "(Ljava/lang/Object;)J");
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "_newarr", "(Ljava/lang/Object;Ljava/lang/Object;J)V");
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(RETURN);
			mv.visitMaxs(4, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC + ACC_NATIVE, "_menter", "(Ljava/lang/Object;II[Ljava/lang/Object;)V", null, null);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "enter", "(II[Ljava/lang/Object;)V", null, null);
			mv.visitCode();
			mv.visitFieldInsn(GETSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "engaged", "I");
			Label l0 = new Label();
			mv.visitJumpInsn(IFEQ, l0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;");
			mv.visitVarInsn(ILOAD, 0);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "_menter", "(Ljava/lang/Object;II[Ljava/lang/Object;)V");
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(RETURN);
			mv.visitMaxs(4, 3);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC + ACC_NATIVE, "_mexit", "(Ljava/lang/Object;IILjava/lang/Object;)V", null, null);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "exit", "(IILjava/lang/Object;)V", null, null);
			mv.visitCode();
			mv.visitFieldInsn(GETSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "engaged", "I");
			Label l0 = new Label();
			mv.visitJumpInsn(IFEQ, l0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;");
			mv.visitVarInsn(ILOAD, 0);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "_mexit", "(Ljava/lang/Object;IILjava/lang/Object;)V");
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(RETURN);
			mv.visitMaxs(4, 3);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC + ACC_NATIVE, "_mexcept", "(Ljava/lang/Object;IILjava/lang/Object;)V", null, null);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "except", "(IILjava/lang/Object;)V", null, null);
			mv.visitCode();
			mv.visitFieldInsn(GETSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "engaged", "I");
			Label l0 = new Label();
			mv.visitJumpInsn(IFEQ, l0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;");
			mv.visitVarInsn(ILOAD, 0);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "_mexcept", "(Ljava/lang/Object;IILjava/lang/Object;)V");
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(RETURN);
			mv.visitMaxs(4, 3);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC + ACC_NATIVE, "_main", "(Ljava/lang/Object;II)V", null, null);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "(II)V", null, null);
			mv.visitCode();
			mv.visitFieldInsn(GETSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "engaged", "I");
			Label l0 = new Label();
			mv.visitJumpInsn(IFEQ, l0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;");
			mv.visitVarInsn(ILOAD, 0);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "_main", "(Ljava/lang/Object;II)V");
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(RETURN);
			mv.visitMaxs(3, 2);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC, "id", "(Ljava/lang/Object;)J", null, null);
			mv.visitCode();
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "getTracker", "()Lnz/ac/vuw/ecs/rprof/HeapTracker;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "nz/ac/vuw/ecs/rprof/HeapTracker", "newId", "()J");
			mv.visitInsn(LRETURN);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC, "getTracker", "()Lnz/ac/vuw/ecs/rprof/HeapTracker;", null, null);
			mv.visitCode();
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE, 0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;");
			mv.visitVarInsn(ASTORE, 1);
			mv.visitVarInsn(ALOAD, 1);
			Label l0 = new Label();
			mv.visitJumpInsn(IFNULL, l0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "_getTracker", "(Ljava/lang/Thread;)Lnz/ac/vuw/ecs/rprof/HeapTracker;");
			mv.visitVarInsn(ASTORE, 0);
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_APPEND, 2, new Object[]{"nz/ac/vuw/ecs/rprof/HeapTracker", "java/lang/Thread"}, 0, null);
			mv.visitVarInsn(ALOAD, 0);
			Label l1 = new Label();
			mv.visitJumpInsn(IFNONNULL, l1);
			mv.visitFieldInsn(GETSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "nullCounter", "Lnz/ac/vuw/ecs/rprof/HeapTracker;");
			mv.visitVarInsn(ASTORE, 0);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(1, 2);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "_getTracker", "(Ljava/lang/Thread;)Lnz/ac/vuw/ecs/rprof/HeapTracker;", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "java/lang/Thread", "_rprof", "Lnz/ac/vuw/ecs/rprof/HeapTracker;");
			mv.visitInsn(ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "_setTracker", "(Ljava/lang/Thread;Lnz/ac/vuw/ecs/rprof/HeapTracker;)V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitFieldInsn(PUTFIELD, "java/lang/Thread", "_rprof", "Lnz/ac/vuw/ecs/rprof/HeapTracker;");
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC + ACC_SYNCHRONIZED, "nextThreadId", "()S", null, null);
			mv.visitCode();
			mv.visitFieldInsn(GETSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "nextThreadId", "S");
			mv.visitVarInsn(ISTORE, 0);
			mv.visitVarInsn(ILOAD, 0);
			mv.visitVarInsn(ILOAD, 0);
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);
			mv.visitInsn(I2S);
			mv.visitVarInsn(ISTORE, 0);
			mv.visitFieldInsn(PUTSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "nextThreadId", "S");
			mv.visitVarInsn(ILOAD, 0);
			mv.visitInsn(IRETURN);
			mv.visitMaxs(3, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "nextThreadId", "()S");
			mv.visitMethodInsn(INVOKESPECIAL, "nz/ac/vuw/ecs/rprof/HeapTracker", "<init>", "(S)V");
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE, "<init>", "(S)V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(LCONST_0);
			mv.visitFieldInsn(PUTFIELD, "nz/ac/vuw/ecs/rprof/HeapTracker", "counter", "J");
			mv.visitLdcInsn(Type.getType("Lnz/ac/vuw/ecs/rprof/HeapTracker;"));
			mv.visitFieldInsn(GETSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "cnum", "I");
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "newcls", "(Ljava/lang/Object;I[I)V");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitInsn(I2L);
			mv.visitIntInsn(BIPUSH, 32);
			mv.visitInsn(LSHL);
			mv.visitFieldInsn(PUTFIELD, "nz/ac/vuw/ecs/rprof/HeapTracker", "thread", "J");
			mv.visitInsn(RETURN);
			mv.visitMaxs(4, 2);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "newId", "()J", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "nz/ac/vuw/ecs/rprof/HeapTracker", "thread", "J");
			mv.visitInsn(LCONST_0);
			mv.visitInsn(LCMP);
			Label l0 = new Label();
			mv.visitJumpInsn(IFNE, l0);
			mv.visitInsn(LCONST_0);
			Label l1 = new Label();
			mv.visitJumpInsn(GOTO, l1);
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "nz/ac/vuw/ecs/rprof/HeapTracker", "thread", "J");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(DUP);
			mv.visitFieldInsn(GETFIELD, "nz/ac/vuw/ecs/rprof/HeapTracker", "counter", "J");
			mv.visitInsn(LCONST_1);
			mv.visitInsn(LADD);
			mv.visitInsn(DUP2_X1);
			mv.visitFieldInsn(PUTFIELD, "nz/ac/vuw/ecs/rprof/HeapTracker", "counter", "J");
			mv.visitInsn(LOR);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{Opcodes.LONG});
			mv.visitInsn(LRETURN);
			mv.visitMaxs(7, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
			mv.visitCode();
			mv.visitInsn(ICONST_0);
			mv.visitFieldInsn(PUTSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "engaged", "I");
			mv.visitInsn(ICONST_1);
			mv.visitFieldInsn(PUTSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "nextThreadId", "S");
			mv.visitTypeInsn(NEW, "nz/ac/vuw/ecs/rprof/HeapTracker");
			mv.visitInsn(DUP);
			mv.visitInsn(ICONST_0);
			mv.visitMethodInsn(INVOKESPECIAL, "nz/ac/vuw/ecs/rprof/HeapTracker", "<init>", "(S)V");
			mv.visitFieldInsn(PUTSTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "nullCounter", "Lnz/ac/vuw/ecs/rprof/HeapTracker;");
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "_rprof_agent_init", "()V");
			mv.visitInsn(RETURN);
			mv.visitMaxs(3, 0);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_STATIC, "_rprof_agent_init", "()V", null, null);
			mv.visitCode();
			mv.visitLdcInsn(Type.getType("Lnz/ac/vuw/ecs/rprof/HeapTracker;"));
			mv.visitInsn(ICONST_1);
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, "nz/ac/vuw/ecs/rprof/HeapTracker", "newcls", "(Ljava/lang/Object;I[I)V");
			mv.visitInsn(RETURN);
			mv.visitMaxs(3, 0);
			mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}
}
