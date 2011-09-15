package nz.ac.vuw.ecs.rprofs.server.weaving;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.util.ASMifierClassVisitor;

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 15/09/11
 */
public class WeaverTestBase {

	private TestingClassLoader loader = new TestingClassLoader();

	protected <T> Class<T> loadClass(String name, byte[] data) {
		return loader.loadClass(name, data);
	}

	protected void print(byte[] cls, OutputStream out) {
		ClassReader r = new ClassReader(cls);
		ClassVisitor w = new ASMifierClassVisitor(new PrintWriter(out));
		r.accept(w, ClassReader.SKIP_DEBUG);
	}

	private class TestingClassLoader extends ClassLoader {
		@SuppressWarnings("unchecked")
		public <T> Class<T> loadClass(String name, byte[] data) {
			return (Class<T>) defineClass(name, data, 0, data.length);
		}
	}
}
