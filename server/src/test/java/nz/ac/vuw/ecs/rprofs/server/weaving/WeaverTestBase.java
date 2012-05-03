package nz.ac.vuw.ecs.rprofs.server.weaving;

import java.io.OutputStream;
import java.io.PrintWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;

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
		r.accept(new TraceClassVisitor(null,
				new ASMifier(),
				new PrintWriter(out)), ClassReader.SKIP_DEBUG);
	}

	private class TestingClassLoader extends ClassLoader {
		@SuppressWarnings("unchecked")
		public <T> Class<T> loadClass(String name, byte[] data) {
			return (Class<T>) defineClass(name, data, 0, data.length);
		}
	}
}
