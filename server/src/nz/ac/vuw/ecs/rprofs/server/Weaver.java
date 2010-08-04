package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.ac.vuw.ecs.rprof.HeapTracker;
import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.weaving.ClassVisitorDispatcher;
import nz.ac.vuw.ecs.rprofs.server.weaving.GenericClassWeaver;
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

		try {
			Context.getInstance().storeClassRecord(cr);
		} catch (NullPointerException ex) {
			ex.printStackTrace();
			System.err.println("Null pointer when storing class, exiting");
			System.exit(1);
		}
	}

	public byte[] weave(byte[] classfile, ClassRecord cr) {
		ClassReader reader = new ClassReader(classfile);
		ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);

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
			else {
				cv = new GenericClassWeaver(cv, cr);
			}
			
			setClassVisitor(cv);
			cv.visit(version, access, name, signature, superName, interfaces);
		}
	}

	/*
	private static class StandardMethodWeaver extends MethodWeaver {

		public StandardMethodWeaver(MethodVisitor mv, MethodRecord mr) {
			super(mv, mr);
		}

		@Override
		public void visitTypeInsn(int opcode, String type) {
			super.visitTypeInsn(opcode, type);

			switch (opcode) {
			case ANEWARRAY:
				insertArrayNotification();
				break;
			}
		}

		@Override
		public void visitIntInsn(int opcode, int operand) {
			super.visitIntInsn(opcode, operand);
			switch (opcode) {
			case NEWARRAY:
				insertArrayNotification();
				break;
			}
		}

		@Override
		public void visitMultiANewArrayInsn(String desc, int dims) {
			super.visitMultiANewArrayInsn(desc, dims);
			insertArrayNotification();
		}

		private void insertArrayNotification() {
			visitInsn(DUP);
			visitMethodInsn(INVOKESTATIC, Type.getInternalName(TRACKER), newarr.getName(), Type.getMethodDescriptor(newarr));
		}

		protected void sVisitTypeInsn(int opcode, String type) {
			super.visitTypeInsn(opcode, type);
		}

		@Override
		public void visitCode() {
			List<Integer> args = getArgs(record.desc);

			super.visitCode();												// stack:
			push(record.parent.id);											// stack: cnum
			push(record.id);												// stack: cnum, mnum
			push(args.size());												// stack: cnum, mnum, numArgs
			sVisitTypeInsn(ANEWARRAY, Type.getInternalName(Object.class));	// stack: cnum, mnum, args

			for (int i = 0; i < args.size(); i++) {
				visitInsn(DUP);												// stack: cnum, mnum, args, args
				push(i);													// stack: cnum, mnum, args, args, i
				visitVarInsn(ALOAD, args.get(i));							// stack: cnum, mnum, args, args, i, val
				visitInsn(AASTORE);											// stack: cnum, mnum, args
			}

			visitMethodInsn(INVOKESTATIC, Type.getInternalName(TRACKER), enter.getName(), Type.getMethodDescriptor(enter));
		}

		private List<Integer> getArgs(String signature) {
			List<Integer> useful = new ArrayList<Integer>(); 
			int argIndex = 0;

			// mark 'this'
			useful.add(0);
			argIndex++;

			outer:
				for (int i = 0; i < signature.length(); i++) {
					switch(signature.charAt(i)) {
					case ')': break outer;
					case 'J': // long
					case 'D': // double
						argIndex++;
					case 'B': // byte
					case 'C': // character
					case 'S': // short
					case 'Z': // boolean
					case 'F': // float
					case 'I': // int
						argIndex++;
						break;
					case 'L': // object, followed by FQ name, terminated by ';'
					case '[': // array, followed by a second type descriptor
						useful.add(argIndex++);
						while (signature.charAt(i) == '[') i++;
						if (signature.charAt(i) == 'L') {
							while (signature.charAt(++i) != ';');
						}
						break;
					}
				}

			return useful;
		}

		@Override
		public void visitInsn(int opcode) {
			switch (opcode) {
			case IRETURN:
			case LRETURN:
			case FRETURN:
			case DRETURN:
			case ARETURN:
			case RETURN:
				push(record.parent.id);
				push(record.id);
				visitMethodInsn(INVOKESTATIC, Type.getInternalName(TRACKER), exit.getName(), Type.getMethodDescriptor(exit));
				break;
			}

			super.visitInsn(opcode);
		}
	}*/
}
