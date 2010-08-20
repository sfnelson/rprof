package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.ac.vuw.ecs.rprof.HeapTracker;
import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.server.weaving.ClassVisitorDispatcher;
import nz.ac.vuw.ecs.rprofs.server.weaving.FieldReader;
import nz.ac.vuw.ecs.rprofs.server.weaving.GenericClassWeaver;
import nz.ac.vuw.ecs.rprofs.server.weaving.ObjectClassWeaver;
import nz.ac.vuw.ecs.rprofs.server.weaving.ThreadClassWeaver;
import nz.ac.vuw.ecs.rprofs.server.weaving.TrackingClassWeaver;

import com.google.gwt.dev.asm.ClassReader;
import com.google.gwt.dev.asm.ClassVisitor;
import com.google.gwt.dev.asm.ClassWriter;
import com.google.gwt.dev.asm.Type;


/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class Weaver extends HttpServlet {

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {

		ClassRecord cr = Context.getInstance().createClassRecord();

		Map<String, String> headers = new HashMap<String, String>();
		for (Enumeration<String> e = req.getHeaderNames(); e.hasMoreElements();) {
			String key = e.nextElement();
			headers.put(key, req.getHeader(key));
		}
		int length = req.getContentLength();

		byte[] buffer = new byte[length];
		InputStream is = req.getInputStream();
		for (int i = 0; i < buffer.length;) {
			i += is.read(buffer, i, buffer.length - i);
		}

		buffer = weave(buffer, cr);

		resp.setStatus(200);
		resp.setContentLength(buffer.length);
		resp.setContentType("application/rprof");
		resp.getOutputStream().write(buffer);
		
		LogRecord record = LogRecord.create();
		record.event = LogRecord.CLASS_WEAVE;
		record.cnum = cr.id;
		record.args = new long[0];
		Context.getInstance().storeLogs(Arrays.asList(record));
	}

	public byte[] weave(byte[] classfile, ClassRecord cr) {
		ClassReader reader = new ClassReader(classfile);
		ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);

		reader.accept(new FieldReader(cr), ClassReader.SKIP_CODE);
		reader.accept(new Dispatcher(writer, cr), ClassReader.EXPAND_FRAMES);
		return writer.toByteArray();
	}
	
	private static class Dispatcher extends ClassVisitorDispatcher {

		private ClassVisitor cv;
		private ClassRecord cr;
		
		public Dispatcher(ClassVisitor cv, ClassRecord cr) {
			this.cv = cv;
			this.cr = cr;
		}

		@Override
		public void visit(int version, int access, String name,
				String signature, String superName, String[] interfaces) {
			
			if (Type.getInternalName(HeapTracker.class).equals(name)) {
				cv = new TrackingClassWeaver(cv, cr);
			}
			else if (Type.getInternalName(Thread.class).equals(name)) {
				cv = new ThreadClassWeaver(cv, cr);
			}
			else if (Type.getInternalName(Object.class).equals(name)) {
				cv = new ObjectClassWeaver(cv, cr);
			}
			else {
				cv = new GenericClassWeaver(cv, cr);
			}
			
			setClassVisitor(cv);
			cv.visit(version, access, name, signature, superName, interfaces);
		}
	}
}
