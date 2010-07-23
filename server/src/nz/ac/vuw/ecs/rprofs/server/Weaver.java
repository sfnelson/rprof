package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.ac.vuw.ecs.rprof.HeapTracker;
import nz.ac.vuw.ecs.rprofs.client.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.client.data.MethodRecord;

import com.google.gwt.dev.asm.ClassAdapter;
import com.google.gwt.dev.asm.ClassReader;
import com.google.gwt.dev.asm.ClassVisitor;
import com.google.gwt.dev.asm.ClassWriter;
import com.google.gwt.dev.asm.FieldVisitor;
import com.google.gwt.dev.asm.MethodVisitor;
import com.google.gwt.dev.asm.Opcodes;
import com.google.gwt.dev.asm.Type;
import com.google.gwt.dev.asm.commons.GeneratorAdapter;


/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class Weaver extends HttpServlet {

	static ArrayList<ClassRecord> classes = new ArrayList<ClassRecord>();

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {

		ClassRecord cr = new ClassRecord();
		cr.id = Weaver.classes.size();
		Weaver.classes.add(cr.id, cr);

		cr.request = req.getRequestURL().substring(8);
		cr.requestLength = req.getContentLength();
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

		cr.responseLength = buffer.length;
	}

	public byte[] weave(byte[] classfile, ClassRecord cr) {
		ClassReader reader = new ClassReader(classfile);
		ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);

		reader.accept(new ClassWeaver(writer, cr), ClassReader.EXPAND_FRAMES);
		return writer.toByteArray();
	}

	private static class ClassWeaver extends ClassAdapter {

		private ClassRecord record;
		private boolean isTracker = false;
		private boolean doOnce = true;

		public ClassWeaver(ClassVisitor cv, ClassRecord record) {
			super(cv);

			this.record = record;
		}

		@Override
		public void visit(int version, int access, String name,
				String signature, String superName, String[] interfaces) {
			record.init(version, access, name, signature, superName, interfaces);

			if (Type.getInternalName(HeapTracker.class).equals(name)) {
				isTracker = true;
			}
			super.visit(version, access, name, signature, superName, interfaces);
		}

		@Override
		public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {

			if (record.name.equals(Type.getInternalName(Thread.class)) && doOnce) {
				doOnce = false;
				Type t = Type.getType(HeapTracker.class);
				FieldVisitor fv = super.visitField(Opcodes.ACC_PUBLIC, "_rprof", t.getDescriptor(), null, null);
				if (fv != null) {
					fv.visitEnd();
				}
			}

			return super.visitField(access, name, desc, signature, value);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc,
				String signature, String[] exceptions) {
			MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
			MethodRecord mr = MethodRecord.create(record, access, name, desc, signature, exceptions);
			if (isTracker) {
				if (name.equals("_getTracker")) {
					mv = new GetTrackerGenerator(mv, mr);
				}
				else if (name.equals("_setTracker")) {
					mv = new SetTrackerGenerator(mv, mr);
				}
			} else if (name.equals("main")
					&& "([Ljava/lang/String;)V".equals(desc)
					&& (Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC) == access) {
				mv = new MainMethodWeaver(mv, mr);
			} else if ((Opcodes.ACC_STATIC & access) == 0
					&& (Opcodes.ACC_PUBLIC & access) != 0
					&& !name.equals("<init>")
					&& !name.equals("<clinit>")) {
				mv = new StandardMethodWeaver(mv, mr);
			} else if (name.equals("<init>")) {
				mv = new InitMethodWeaver(mv, mr);
			}
			return mv;
		}

	}

	private static class MethodWeaver extends GeneratorAdapter implements Opcodes {

		protected static final Class<HeapTracker> TRACKER = HeapTracker.class;

		protected static final Method enter = getTrackerMethod("enter");
		protected static final Method exit = getTrackerMethod("exit");
		protected static final Method newarr = getTrackerMethod("newarr");
		protected static final Method newobj = getTrackerMethod("newobj");

		protected MethodRecord record;

		public MethodWeaver(MethodVisitor mv, MethodRecord mr) {
			super(mv, mr.access, mr.name, mr.desc);

			this.record = mr;
		}

		protected static Method getTrackerMethod(String name) {
			try {
				Method[] methods = TRACKER.getMethods();
				Method e = null;
				for (Method m: methods) {
					if (m.getName() == name) {
						e = m;
						break;
					}
				}
				if (e == null) {
					throw new NoSuchMethodException();
				} else {
					return e;
				}
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}

		}
	}

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


		/**
		 * Visits a type instruction ({@link MethodVisitor#visitTypeInsn(int, String)})
		 * without adding tracking code.
		 */
		protected void sVisitTypeInsn(int opcode, String type) {
			super.visitTypeInsn(opcode, type);
		}

		@Override
		public void visitCode() {
			List<Integer> args = getArgs(record.desc);

			super.visitCode();												// stack:
			push(record.parent.id());										// stack: cnum
			push(record.id());												// stack: cnum, mnum
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
				push(record.parent.id());
				push(record.id());
				visitMethodInsn(INVOKESTATIC, Type.getInternalName(TRACKER), exit.getName(), Type.getMethodDescriptor(exit));
				break;
			}

			super.visitInsn(opcode);
		}
	}

	private static class InitMethodWeaver extends MethodWeaver {

		public InitMethodWeaver(MethodVisitor mv, MethodRecord mr) {
			super(mv, mr);
		}

		@Override
		public void visitCode() {
			super.visitCode();
			visitIntInsn(ALOAD, 0);
			visitMethodInsn(INVOKESTATIC, Type.getInternalName(TRACKER), newobj.getName(), Type.getMethodDescriptor(newobj));
		}
	}

	private static class MainMethodWeaver extends MethodWeaver {

		protected static final Method main = getTrackerMethod("main");
		protected static final Method exit = getTrackerMethod("exit");

		public MainMethodWeaver(MethodVisitor mv, MethodRecord mr) {
			super(mv, mr);
		}

		@Override
		public void visitCode() {
			super.visitCode();
			push(record.parent.id());
			push(record.id());
			visitMethodInsn(INVOKESTATIC, Type.getInternalName(TRACKER), main.getName(), Type.getMethodDescriptor(main));
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
				push(record.parent.id());
				push(record.id());
				visitMethodInsn(INVOKESTATIC, Type.getInternalName(TRACKER), exit.getName(), Type.getMethodDescriptor(exit));
				break;
			}

			super.visitInsn(opcode);
		}
	}

	private static class GetTrackerGenerator extends GeneratorAdapter implements Opcodes {

		public static final Method ct;

		static {
			try {
				ct = Thread.class.getMethod("currentThread");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public GetTrackerGenerator(MethodVisitor mv, MethodRecord mr) {
			super(mv, mr.access, mr.name, mr.desc);
		}

		@Override
		public void visitCode() {
			super.visitCode();

			//return Thread.currentThread()._rprof;

			// locals: [thread]
			// stack:  []
			
			super.visitVarInsn(ALOAD, 0);
			
			// locals: [thread]
			// stack:  [thread]
			
			Type t = Type.getType(HeapTracker.class);
			super.visitFieldInsn(GETFIELD, Type.getInternalName(Thread.class), "_rprof", t.getDescriptor());
			
			// locals: [thread]
			// stack:  [tracker]
			
			super.visitInsn(ARETURN);
			
			super.visitMaxs(1, 1);
			
			super.visitEnd();
		}
	}

	private static class SetTrackerGenerator extends GeneratorAdapter implements Opcodes {

		public SetTrackerGenerator(MethodVisitor mv, MethodRecord mr) {
			super(mv, mr.access, mr.name, mr.desc);
		}

		@Override
		public void visitCode() {
			super.visitCode();

			// Thread.currentThread()._rprof = c; // c is first argument

			// locals: [thread, tracker]
			// stack:  []

			super.visitVarInsn(ALOAD, 0);
			
			// locals: [thread, tracker]
			// stack:  [thread]

			super.visitVarInsn(ALOAD, 1);

			// locals: [thread, tracker]
			// stack:  [thread, tracker]

			Type t = Type.getType(HeapTracker.class);
			super.visitFieldInsn(PUTFIELD, Type.getInternalName(Thread.class), "_rprof", t.getDescriptor());
			
			// locals: [thread, tracker]
			// stack:  []			
			
			super.visitMaxs(2, 2);
			
			super.visitEnd();
		}
	}
}
