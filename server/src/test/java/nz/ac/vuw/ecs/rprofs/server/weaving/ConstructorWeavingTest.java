package nz.ac.vuw.ecs.rprofs.server.weaving;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.RETURN;

import java.io.PrintWriter;

import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.ASMifierClassVisitor;

public class ConstructorWeavingTest {

	private TestingClassLoader loader;

	@Before
	public void setUp() throws Exception {
		loader = new TestingClassLoader();
	}

	@Test
	public void test() throws InstantiationException, IllegalAccessException {
		byte[] pre = generateMinimalClass("TestInput");
		loader.loadClass("TestInput", pre).newInstance();

		byte[] post = generateMinimalClass("TestOutput");
		Weaver w = new Weaver(new ClassId((short) 0, 1));
		post = w.weave(post);
		//print(post);
		loader.loadClass("TestOutput", post).newInstance();
	}

	@SuppressWarnings("unused")
	private static void print(byte[] cls) {
		ClassReader r = new ClassReader(cls);
		ClassVisitor w = new ASMifierClassVisitor(new PrintWriter(System.out));
		r.accept(w, 2);
	}

	private class TestingClassLoader extends ClassLoader {

		@SuppressWarnings("unchecked")
		public <T> Class<T> loadClass(String name, byte[] data) {
			return (Class<T>) defineClass(name, data, 0, data.length);
		}
	}

	public static byte[] generateMinimalClass(String name) {

		ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;

		cw.visit(51, ACC_PUBLIC + ACC_SUPER, name, null, "java/lang/Object", null);

		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
			mv.visitInsn(RETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}

		cw.visitEnd();

		return cw.toByteArray();
	}

	public static byte[] generateExceptionClass () throws Exception {

		ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;

		cw.visit(51, ACC_PUBLIC + ACC_SUPER, "Test", null, "java/lang/Object", null);

		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			Label l1 = new Label();
			Label l2 = new Label();
			mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Exception");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitLdcInsn("Oh hai");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
			mv.visitLabel(l1);
			Label l3 = new Label();
			mv.visitJumpInsn(GOTO, l3);
			mv.visitLabel(l2);
			mv.visitFrame(Opcodes.F_FULL, 1, new Object[] {"Test"}, 1, new Object[] {"java/lang/Exception"});
			mv.visitVarInsn(ASTORE, 1);
			mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/Throwable;)V");
			mv.visitInsn(ATHROW);
			mv.visitLabel(l3);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(RETURN);
			mv.visitMaxs(3, 2);
			mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}
}
