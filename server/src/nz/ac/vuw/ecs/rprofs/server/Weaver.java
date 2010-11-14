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
import nz.ac.vuw.ecs.rprofs.server.data.Context;
import nz.ac.vuw.ecs.rprofs.server.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.server.data.Context.ActiveContext;
import nz.ac.vuw.ecs.rprofs.server.weaving.ClassVisitorDispatcher;
import nz.ac.vuw.ecs.rprofs.server.weaving.FieldReader;
import nz.ac.vuw.ecs.rprofs.server.weaving.GenericClassWeaver;
import nz.ac.vuw.ecs.rprofs.server.weaving.ObjectClassWeaver;
import nz.ac.vuw.ecs.rprofs.server.weaving.ThreadClassWeaver;
import nz.ac.vuw.ecs.rprofs.server.weaving.TrackingClassWeaver;

import com.google.gwt.dev.asm.ClassAdapter;
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

		ActiveContext context = Context.getCurrent();
		
		ClassRecord cr = context.createClassRecord();

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

		buffer = weave(context, buffer, cr);

		resp.setStatus(200);
		resp.setContentLength(buffer.length);
		resp.setContentType("application/rprof");
		resp.getOutputStream().write(buffer);
		
		LogRecord record = LogRecord.create();
		record.event = LogRecord.CLASS_WEAVE;
		record.cnum = cr.getId();
		record.args = new long[0];
		context.storeLogs(Arrays.asList(record));
	}

	public byte[] weave(ActiveContext context, byte[] classfile, ClassRecord cr) {
		ClassReader reader = new ClassReader(classfile);
		ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);

		reader.accept(new FieldReader(context, cr), ClassReader.SKIP_CODE);
		reader.accept(new Dispatcher(context, writer, cr), ClassReader.EXPAND_FRAMES);
		
		return writer.toByteArray();
	}
	
	private static class Dispatcher extends ClassVisitorDispatcher {

		private ActiveContext context;
		private ClassVisitor cv;
		private ClassRecord cr;
		
		public Dispatcher(ActiveContext context, ClassVisitor cv, ClassRecord cr) {
			this.context = context;
			this.cv = cv;
			this.cr = cr;
		}

		@Override
		public void visit(int version, int access, String name,
				String signature, String superName, String[] interfaces) {
			
			ClassVisitor cv = null;
			
			
			String[] filters = new String[] {
				"sun/reflect/.*",	// jhotdraw crashes in this package
				"java/awt/.*",		// jhotdraw has font problems if this packages is included
				"com/sun/.*",
				"sun/.*",
				"apple/.*",
				"com/apple/.*",		// might help jhotdraw?
				"java/lang/IncompatibleClassChangeError",	// gc blows up if this is woven
				"java/lang/LinkageError",					// gc blows up if this is woven
				"java/lang/NullPointerException"			// something blows up - null pointers appear as runtime exceptions with this change
			};
			
			int flags = cr.getFlags();
			
			if (Type.getInternalName(HeapTracker.class).equals(name)) {
				flags |= ClassRecord.SPECIAL_CLASS_WEAVER;
				cv = new TrackingClassWeaver(context, this.cv, cr);
			}
			else if (Type.getInternalName(Thread.class).equals(name)) {
				flags |= ClassRecord.SPECIAL_CLASS_WEAVER;
				cv = new ThreadClassWeaver(context, this.cv, cr);
			}
			else if (Type.getInternalName(Throwable.class).equals(name)) {
				flags |= ClassRecord.SPECIAL_CLASS_WEAVER;
				cv = new ThreadClassWeaver(context, this.cv, cr);
			}
			else if (Type.getInternalName(Object.class).equals(name)) {
				flags |= ClassRecord.SPECIAL_CLASS_WEAVER;
				cv = new ObjectClassWeaver(context, this.cv, cr);
			}
			else {
				for (String filter: filters) {
					if (name.matches(filter)) {
						flags |= ClassRecord.CLASS_IGNORED_PACKAGE_FILTER;
						cv = new ClassAdapter(this.cv);
					}
				}
			}
			
			cr.setFlags(flags);
			
			if (cv == null) {
				cv = new GenericClassWeaver(context, this.cv, cr);
			}
			
			setClassVisitor(cv);
			cv.visit(version, access, name, signature, superName, interfaces);
		}
	}
}
