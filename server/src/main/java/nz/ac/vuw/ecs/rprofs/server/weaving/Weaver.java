package nz.ac.vuw.ecs.rprofs.server.weaving;

import java.util.regex.Pattern;

import nz.ac.vuw.ecs.rprof.HeapTracker;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Weaver {

	private static final Logger log = LoggerFactory.getLogger(Weaver.class);

	public static final Pattern includes = new PatternBuilder()
			.add(".*")
			.get();

	public static final Pattern excludes = new PatternBuilder()
			.add("sun/reflect/.*")	// jhotdraw crashes in this package
			.add("java/awt/.*")		// jhotdraw has font problems if this packages is included
			.add("com/sun/.*")
			.add("sun/.*")
			.add("apple/.*")
			.add("com/apple/.*")		// might help jhotdraw?
			.add("java/lang/IncompatibleClassChangeError")	// gc blows up if this is woven
			.add("java/lang/LinkageError")					// gc blows up if this is woven
			.add("java/lang/NullPointerException")			// something blows up - null pointers appear as runtime exceptions with this change
			.add("java/util/concurrent/.*")					// SIGSEGV/SIGBUS in pmd
			.add("java/lang/reflect/.*")
			.add("java/nio/charset/CharsetDecoder")
			.add("java/nio/charset/CharsetEncoder")
			.add("java/util/zip/ZipFile")
			.get();

	public byte[] weave(ClassRecord record, byte[] classfile) {

		ClassReader reader = new ClassReader(classfile);
		ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);

		int flags = record.getProperties();

		ClassVisitor visitor;
		if (Type.getInternalName(HeapTracker.class).equals(record.getName())) {
			flags |= Clazz.SPECIAL_CLASS_WEAVER;
			visitor = new TrackingClassWeaver(writer, record);
		} else if (Type.getInternalName(Thread.class).equals(record.getName())) {
			flags |= Clazz.SPECIAL_CLASS_WEAVER;
			visitor = new ThreadClassWeaver(writer, record);
		} else if (Type.getInternalName(Throwable.class).equals(record.getName())) {
			flags |= Clazz.SPECIAL_CLASS_WEAVER;
			visitor = new ThrowableClassWeaver(writer, record);
		} else if (Type.getInternalName(Object.class).equals(record.getName())) {
			flags |= Clazz.SPECIAL_CLASS_WEAVER;
			visitor = new ObjectClassWeaver(writer, record);
		} else {
			if (includes.matcher(record.getName()).find()) {
				flags |= Clazz.CLASS_INCLUDE_MATCHED;
				if (excludes.matcher(record.getName()).find()) {
					flags |= Clazz.CLASS_EXCLUDE_MATCHED;
					visitor = writer;
				} else {
					visitor = new GenericClassWeaver(writer, record);
				}
			} else {
				visitor = writer;
			}
		}

		record.setProperties(flags);

		reader.accept(visitor, 0);

		return writer.toByteArray();
	}

	private static class PatternBuilder {
		StringBuilder pattern = null;

		public PatternBuilder add(String pattern) {
			if (this.pattern == null) {
				this.pattern = new StringBuilder("^");
			} else {
				this.pattern.append('|');
			}
			this.pattern.append('(');
			this.pattern.append(pattern);
			this.pattern.append(')');
			return this;
		}

		public Pattern get() {
			String pattern = this.pattern.append("$").toString();
			log.trace("using pattern: {}", pattern);
			return Pattern.compile(pattern);
		}
	}
}
