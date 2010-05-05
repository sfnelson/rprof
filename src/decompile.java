import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

abstract class Document {

	protected InputStream in;
	protected PrintStream out;

	public Document(InputStream in, PrintStream out) throws IOException {
		this.in = in;
		this.out = out;

		preamble();

		try {
			content();
		} finally {
			conclusion();
		}
	}

	protected abstract void preamble();
	protected abstract void content() throws IOException;
	protected abstract void conclusion();

	public <T> T add(Node<T> node) throws IOException {
		T value;
		try {
			value = node.read(in);
		} finally {
			print(node);
		}
		return value;
	}

	protected abstract void print(Node<?> node);
}

abstract class HtmlDocument extends Document {

	public HtmlDocument(InputStream in, PrintStream out) throws IOException {
		super(in, out);
	}

	@Override
	public void preamble() {
		String style = "<style>\n"
			+ ".basic, .composite { clear: both }\n"
			+ ".contents { padding-left: 2em }\n"
			+ ".collapse .contents { display: none; }\n"
			+ ".basic span { display: block; float: left; width: 8em; overflow: hidden; margin-right: 2em }\n"
			+ ".basic span.content { width: 24em; }\n"
			+ ".raw { font-family: monospace; }\n"
			+ "</style>\n";
		out.printf("<!DOCTYPE html>\n<html lang='en'>\n  <title>Output</title>\n%s</head>\n<body>\n", style);
	}

	@Override
	public void conclusion() {
		String script = "<script>\n"
			+ "document.body.onclick = function (e) {\n"
			+ "  var n = e.target;\n"
			+ "  while (n && !n.className.match('composite')) n = n.parentNode;\n"
			+ "  if (!n);\n"
			+ "  else if (n.className.match('collapse')) n.className = 'composite';"
			+ "  else n.className += ' collapse';\n"
			+ "}\n"
			+ "</script>\n";
		out.printf("%s</body>\n</html>\n", script);
	}

	@Override
	public void print(Node<?> node) {
		if (node instanceof BasicNode<?>) {
			BasicNode<?> n = (BasicNode<?>) node;
			out.printf("<div class='basic'>"
					+ "<span class='name'>%s</span>"
					+ "<span class='content %s'>%s</span>"
					+ "<span class='raw'>%s</span>"
					+ "</div>\n",
					n.name(), n.type(), n.value(), n.raw());
		} else {
			CompositeNode<?> c = (CompositeNode<?>) node;
			out.printf("<div class='composite'>\n<span class='name'>%s</span>\n<div class='contents'>\n", c.name());
			for (Node<?> n: c) {
				print(n);
			}
			out.printf("</div>\n</div>\n");
		}
	}
}

abstract class Node<T> {
	private final String name;
	public Node(String name) {
		this.name = name;
	}
	public String name() { return name; }
	public abstract T read(InputStream in) throws IOException;
}

abstract class BasicNode<T> extends Node<T> {
	private byte[] raw;
	public BasicNode(String name) {
		super(name);
	}
	public T read(InputStream in) throws IOException {
		raw = new byte[size()];
		in.read(raw);
		return parse(raw);
	}
	protected abstract T parse(byte[] raw);
	protected abstract int size();
	public String raw() {
		String value = "";
		for (byte b : raw) {
			value = String.format("%s%02x", value, b);
		}
		return value;
	}
	public abstract String type();
	public abstract String value();
}

class HexString extends BasicNode<String> {

	private final int length;
	private String value;

	public HexString(String name, int length) {
		super(name);
		this.length = length;
	}

	@Override
	protected String parse(byte[] raw) {
		value = "0x";
		for (byte b : raw) {
			value = String.format("%s%02x", value, b);
		}
		return value;
	}

	@Override protected int size() { return length; }
	@Override public String type() { return "hex"; }
	@Override public String value() { return value.toString(); }
}

class LongNode extends BasicNode<Long> {
	private long value;
	public LongNode(String name) {
		super(name);
	}
	@Override
	protected Long parse(byte[] raw) {
		assert(raw.length == 8);
		value = decompile.parseLong(raw, 0);
		return value;
	}
	@Override protected int size() { return 8; }
	@Override public String type() { return "number"; }
	@Override public String value() { return String.valueOf(value); }
}

class IntegerNode extends BasicNode<Integer> {
	private int value;
	public IntegerNode(String name) {
		super(name);
	}
	@Override
	protected Integer parse(byte[] raw) {
		assert(raw.length == 4);
		value = decompile.parseInteger(raw, 0);
		return value;
	}
	@Override protected int size() { return 4; }
	@Override public String type() { return "number"; }
	@Override public String value() { return String.valueOf(value); }
}

class ShortNode extends BasicNode<Short> {
	private short value;
	public ShortNode(String name) {
		super(name);
	}
	@Override
	protected Short parse(byte[] raw) {
		assert(raw.length == 2);
		value = decompile.parseShort(raw, 0);
		return value;
	}
	@Override protected int size() { return 2; }
	@Override public String type() { return "number"; }
	@Override public String value() { return String.valueOf(value); }
}

class Reference extends BasicNode<Reference> {
	static ConstantPool pool;

	protected short value;
	public Reference(String name) {
		super(name);
	}
	@Override
	protected Reference parse(byte[] raw) {
		assert(raw.length == 2);
		value = decompile.parseShort(raw, 0);
		return this;
	}
	@Override protected int size() { return 2; }
	@Override public String type() { return "number"; }
	@Override public String value() { 
		if (pool != null) {
			return String.valueOf(pool.get(value));
		}
		else {
			return String.valueOf(value);
		}
	}

	@Override public String toString() {
		return value();
	}
}

class ByteReference extends Reference {
	public ByteReference(String name) {
		super(name);
	}
	protected int size() { return 1; }
	protected Reference parse(byte[] raw) {
		assert(raw.length == 1);
		value = (short)(raw[0] & 0xff);
		return this;
	}
}

class ByteNode extends BasicNode<Byte> {
	private byte value;
	public ByteNode(String name) {
		super(name);
	}
	@Override
	protected Byte parse(byte[] raw) {
		assert(raw.length == 1);
		value = raw[0];
		return value;
	}
	@Override protected int size() { return 1; }
	@Override public String type() { return "number"; }
	@Override public String value() { return String.valueOf(value); }
}

class StringNode extends BasicNode<String> {
	private int length;
	private String value;
	public StringNode(String name, int length) {
		super(name);
		this.length = length;
	}
	@Override
	protected String parse(byte[] raw) {
		value = new String(raw);
		return value;
	}
	@Override protected int size() { return length; }
	@Override public String type() { return "number"; }
	@Override public String value() { return String.valueOf(value); }
}

abstract class CompositeNode<T> extends Node<T> implements Iterable<Node<?>> {
	private final List<Node<?>> contents = new ArrayList<Node<?>>();
	public CompositeNode(String name) {
		super(name);
	}
	public <S> S add(Node<S> node, InputStream in) throws IOException {
		S value = node.read(in);
		contents.add(node);
		return value;
	}
	public Iterator<Node<?>> iterator() {
		return contents.iterator();
	}
}

class Constant extends CompositeNode<Constant> {

	String name;
	Object[] values;

	public Constant() {
		super("constant");
	}

	private void store(Object o) {
		values = new Object[] { o };
	}

	private void store(Object[] o) {
		values = o;
	}

	public Constant read(InputStream in) throws IOException {
		byte type = add(new ByteNode("type"), in);
		switch (type) {
		case 1:
			int length = add(new ShortNode("length"), in);
			store(add(new StringNode("Asciz", length), in));
			break;
		case 5:
			store(add(new LongNode("Long"), in));
			break;
		case 7:
			name = "class";
			store(add(new Reference("class"), in));
			break;
		case 8:
			name = "string";
			store(add(new Reference("string"), in));
			break;
		case 9:
			name = "field";
			store(new Object[] { add(new Reference("class"), in), add(new Reference(".field"), in) });
			break;
		case 10:
			name = "method";
			store(new Object[] { add(new Reference("class"), in), add(new Reference("method"), in) });
			break;
		case 11:
			name = "imethod";
			store(new Object[] { add(new Reference("interface"), in), add(new Reference("method"), in) });
			break;
		case 12:
			name = "type";
			store(new Object[] { add(new Reference("name"), in), add(new Reference("type"), in) });
			break;
		default:
			throw new IOException("unknown constant type: " + type);
		}

		return this;
	}

	public String toString() {
		String value = name == null ? "" : name + " ";
		for (Object o: values) {
			value += o.toString();
		}
		return value;
	}
}

class ConstantPool extends CompositeNode<ConstantPool> {

	private final Map<Short, Constant> constants = new HashMap<Short, Constant>();
	private final int length;

	public ConstantPool(int length) {
		super("constants pool");
		this.length = length;
	}

	@Override
	public ConstantPool read(InputStream in) throws IOException {
		for (short i = 1; i < length; i++) {
			Constant c = add(new Constant(), in);
			constants.put(i, c);
		}
		return this;
	}

	public Object get(short id) {
		return constants.get(id);
	}
}

class Interfaces extends CompositeNode<Interfaces> {

	int numInterfaces;

	public Interfaces(int numInterfaces) {
		super("interfaces");
		this.numInterfaces = numInterfaces;
	}

	@Override
	public Interfaces read(InputStream in) throws IOException {
		for (int i = 0; i < numInterfaces; i++) {
			add(new Reference("interface"), in);
		}
		return this;
	}

}

class Fields extends CompositeNode<Fields> {

	int numFields;

	public Fields(int numFields) {
		super("fields");
		this.numFields = numFields;
	}

	@Override
	public Fields read(InputStream in) throws IOException {
		for (int i = 0; i < numFields; i++) {
			add(new Field(), in);
		}
		return this;
	}

}

class Field extends CompositeNode<Field> {

	public Field() {
		super("field");
	}

	@Override
	public Field read(InputStream in) throws IOException {
		add(new HexString("access flags", 2), in);
		add(new Reference("name"), in);
		add(new Reference("descriptor"), in);

		int numAttributes = add(new ShortNode("num attributes"), in);
		add(new Attributes(numAttributes), in);

		return this;
	}

}

class Attributes extends CompositeNode<Attributes> {

	int num;

	public Attributes(int num) {
		super("attributes");
		this.num = num;
	}

	public Attributes read(InputStream in) throws IOException {
		for (int i = 0; i < num; i++) {
			add(new Attribute(), in);
		}
		return this;
	}
}
class Attribute extends CompositeNode<Attribute> {

	public Attribute() {
		super("attribute");
	}

	public Attribute read(InputStream in) throws IOException {
		Reference id = add(new Reference("id"), in);
		int length = add(new IntegerNode("length"), in);

		if (id.value().equals("Code")) {
			add(new CodeAttribute(length), in);
		} else {
			add(new HexString("value", length), in);
		}
		return this;
	}
}

class Methods extends CompositeNode<Methods> {

	int numMethods;

	public Methods(int numMethods) {
		super("methods");
		this.numMethods = numMethods;
	}

	@Override
	public Methods read(InputStream in) throws IOException {
		for (int i = 0; i < numMethods; i++) {
			add(new Method(), in);
		}
		return this;
	}

}

class Method extends CompositeNode<Method> {

	public Method() {
		super("method");
	}

	@Override
	public Method read(InputStream in) throws IOException {
		add(new HexString("access flags", 2), in);
		add(new Reference("name"), in);
		add(new Reference("descriptor"), in);

		int numAttributes = add(new ShortNode("num attributes"), in);
		add(new Attributes(numAttributes), in);

		return this;
	}

}

class CodeAttribute extends CompositeNode<CodeAttribute> {

	int length;

	public CodeAttribute(int length) {
		super("code");
		this.length = length;
	}

	@Override
	public CodeAttribute read(InputStream in) throws IOException {

		add(new ShortNode("max stack"), in);
		add(new ShortNode("max locals"), in);

		int length = add(new IntegerNode("code length"), in);
		add(new CodeBlock(length), in);

		//    	u2 exception_table_length;
		short numExceptions = add(new ShortNode("exceptions length"), in);
		add(new Exceptions(numExceptions), in);
		//    	{    	u2 start_pc;
		//    	      	u2 end_pc;
		//    	      	u2  handler_pc;
		//    	      	u2  catch_type;
		//    	}	exception_table[exception_table_length];

		//    	u2 attributes_count;
		short numAttributes = add(new ShortNode("num attributes"), in);
		//    	attribute_info attributes[attributes_count];
		add(new Attributes(numAttributes), in);
		return null;
	}

}

class Exceptions extends CompositeNode<Exceptions> {

	int num;

	public Exceptions(int num) {
		super("exceptions table");
		this.num = num;
	}

	@Override
	public Exceptions read(InputStream in) throws IOException {
		for (int i = 0; i < num; i++) {
			add(new ExceptionNode(), in);
		}
		return this;
	}
}

class ExceptionNode extends CompositeNode<ExceptionNode> {

	public ExceptionNode() {
		super("exception");
	}

	@Override
	public ExceptionNode read(InputStream in) throws IOException {
		add(new ShortNode("start pc"), in);
		add(new ShortNode("end pc"), in);
		add(new ShortNode("handler pc"), in);
		add(new Reference("catch type"), in);
		return this;
	}
}

class CodeBlock extends CompositeNode<CodeBlock> {

	int length;

	public CodeBlock(int length) {
		super("code");
		this.length = length;
	}

	@Override
	public CodeBlock read(InputStream in) throws IOException {
		for (int i = 0; i < length; i++) {
			OpCode o = add(new OpCode(), in);
			for (BasicNode<?> n: o.op.read()) {
				i += n.size();
				add(n, in);
			}
		}
		return this;
	}

	private class OpCode extends BasicNode<OpCode> {
		private byte value;
		private Op op;

		public OpCode() {
			super("op");
		}
		@Override
		protected OpCode parse(byte[] raw) {
			assert(raw.length == 1);
			value = raw[0];

			int x = (value >> 4) & 0xF;
			int y = value & 0xF;
			op = opcodes[x][y];
			return this;
		}
		@Override protected int size() { return 1; }
		@Override public String type() { return "op"; }
		@Override public String value() {
			if (op.name == null || op.name.length() == 0) return "<i>unknown</i>";

			int index = -1;
			switch (op.name.charAt(0)) {
			case 'a': index = 0; break;
			case 'b': index = 1; break;
			case 'c': index = 2; break;
			case 'd': index = 3; break;
			case 'f': index = 4; break;
			case 'g': index = 5; break;
			case 'i': index = 6; break;
			case 'j': index = 7; break;
			case 'l': index = 8; break;
			case 'm': index = 9; break;
			case 'n': index = 10; break;
			case 'p': index = 11; break;
			case 'r': index = 12; break;
			case 's': index = 13; break;
			case 't': index = 14; break;
			case 'w': index = 15; break;
			}

			if (index >= 0) {
				String address = "http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc";
				if (index > 0) address += index;
				address += ".html#" + op.name + ".Operation";
				return String.format("<a href='%s'>%s</a>", address, op.name);
			} else {
				return op.name;
			}
		}
	}

	private class Op {
		public String name;
		public Op(String name) {
			this.name = name;
		}
		BasicNode<?>[] read() {
			return new BasicNode[0];
		}
	}
	
	private class ReferenceOp extends Op {
		String type;
		public ReferenceOp(String name, String type) {
			super(name);
			this.type = type;
		}
		BasicNode<?>[] read() {
			return new BasicNode[] {
				new Reference(type)
			};
		}
	}
	
	private class ShortOp extends Op {
		String type;
		public ShortOp(String name, String type) {
			super(name);
			this.type = type;
		}
		BasicNode<?>[] read() {
			return new BasicNode[] {
				new ShortNode(type)
			};
		}
	}
	
	private class ByteOp extends Op {
		String type;
		public ByteOp(String name, String type) {
			super(name);
			this.type = type;
		}
		BasicNode<?>[] read() {
			return new BasicNode[] {
				new ByteNode(type)
			};
		}
	}
	
	private class ByteRefOp extends Op {
		String type;
		public ByteRefOp(String name, String type) {
			super(name);
			this.type = type;
		}
		BasicNode<?>[] read() {
			return new BasicNode[] {
				new ByteReference(type)
			};
		}
	}

	Op [][] opcodes = {
		{
			new Op("nop"),
			new Op("aconst_null"), 
			new Op("iconst_m1"), 
			new Op("iconst_0"), 
			new Op("iconst_1"), 
			new Op("iconst_2"), 
			new Op("iconst_3"), 
			new Op("iconst_4"), 
			new Op("iconst_5"), 
			new Op("lconst_0"), 
			new Op("lconst_1"), 
			new Op("fconst_0"), 
			new Op("fconst_1"), 
			new Op("fconst_2"), 
			new Op("dconst_0"), 
			new Op("dconst_1"), 
		},
		{
			new ByteOp("bipush", "byte"),
			new Op("sipush"), 
			new ByteRefOp("ldc", "index"), 
			new Op("ldc_w"), 
			new Op("ldc2_w"), 
			new Op("iload"), 
			new Op("lload"), 
			new Op("fload"), 
			new Op("dload"), 
			new Op("aload"), 
			new Op("iload_0"), 
			new Op("iload_1"), 
			new Op("iload_2"), 
			new Op("iload_3"), 
			new Op("lload_0"), 
			new Op("lload_1") 
		},
		{
			new Op("lload_2"), 
			new Op("lload_3"), 
			new Op("fload_0"), 
			new Op("fload_1"), 
			new Op("fload_2"), 
			new Op("fload_3"), 
			new Op("dload_0"), 
			new Op("dload_1"), 
			new Op("dload_2"), 
			new Op("dload_3"), 
			new Op("aload_0"), 
			new Op("aload_1"), 
			new Op("aload_2"), 
			new Op("aload_3"), 
			new Op("iaload"), 
			new Op("laload"), 
		} ,
		{ 
			new Op("faload"), 
			new Op("daload"), 
			new Op("aaload"), 
			new Op("baload"), 
			new Op("caload"), 
			new Op("saload"), 
			new Op("istore"), 
			new Op("lstore"), 
			new Op("fstore"), 
			new Op("dstore"), 
			new Op("astore"), 
			new Op("istore_0"), 
			new Op("istore_1"), 
			new Op("istore_2"), 
			new Op("istore_3"), 
			new Op("lstore_0"), 
		} ,
		{ 
			new Op("lstore_1"), 
			new Op("lstore_2"), 
			new Op("lstore_3"), 
			new Op("fstore_0"), 
			new Op("fstore_1"), 
			new Op("fstore_2"), 
			new Op("fstore_3"), 
			new Op("dstore_0"), 
			new Op("dstore_1"), 
			new Op("dstore_2"), 
			new Op("dstore_3"), 
			new Op("astore_0"), 
			new Op("astore_1"), 
			new Op("astore_2"), 
			new Op("astore_3"), 
			new Op("iastore"), 
		} ,
		{ 
			new Op("lastore"), 
			new Op("fastore"), 
			new Op("dastore"), 
			new Op("aastore"), 
			new Op("bastore"), 
			new Op("castore"), 
			new Op("sastore"), 
			new Op("pop"), 
			new Op("pop2"), 
			new Op("dup"), 
			new Op("dup_x1"), 
			new Op("dup_x2"), 
			new Op("dup2"), 
			new Op("dup2_x1"), 
			new Op("dup2_x2"), 
			new Op("swap"), 
		} ,
		{
			new Op("iadd"), 
			new Op("ladd"), 
			new Op("fadd"), 
			new Op("dadd"), 
			new Op("isub"), 
			new Op("lsub"), 
			new Op("fsub"), 
			new Op("dsub"), 
			new Op("imul"), 
			new Op("lmul"), 
			new Op("fmul"), 
			new Op("dmul"), 
			new Op("idiv"), 
			new Op("ldiv"), 
			new Op("fdiv"), 
			new Op("ddiv"), 
		} ,
		{
			new Op("irem"), 
			new Op("lrem"), 
			new Op("frem"), 
			new Op("drem"), 
			new Op("ineg"), 
			new Op("lneg"), 
			new Op("fneg"), 
			new Op("dneg"), 
			new Op("ishl"), 
			new Op("lshl"), 
			new Op("ishr"), 
			new Op("lshr"), 
			new Op("iushr"), 
			new Op("lushr"), 
			new Op("iand"), 
			new Op("land"), 
		} ,
		{
			new Op("ior"), 
			new Op("lor"), 
			new Op("ixor"), 
			new Op("lxor"), 
			new Op("iinc"), 
			new Op("i2l"), 
			new Op("i2f"), 
			new Op("i2d"), 
			new Op("l2i"), 
			new Op("l2f"), 
			new Op("l2d"), 
			new Op("f2i"), 
			new Op("f2l"), 
			new Op("f2d"), 
			new Op("d2i"), 
			new Op("d2l"), 
		} ,
		{
			new Op("d2f"), 
			new Op("i2b"), 
			new Op("i2c"), 
			new Op("i2s"), 
			new Op("lcmp"), 
			new Op("fcmpl"), 
			new Op("fcmpg"), 
			new Op("dcmpl"), 
			new Op("dcmpg"), 
			new ShortOp("ifeq", "offset"), 
			new ShortOp("ifne", "offset"), 
			new ShortOp("iflt", "offset"), 
			new ShortOp("ifge", "offset"), 
			new ShortOp("ifgt", "offset"), 
			new ShortOp("ifle", "offset"), 
			new ShortOp("if_icmpeq", "offset"), 
		} ,
		{
			new ShortOp("if_icmpne", "offset"), 
			new ShortOp("if_icmplt", "offset"), 
			new ShortOp("if_icmpge", "offset"), 
			new ShortOp("if_icmpgt", "offset"), 
			new ShortOp("if_icmple", "offset"), 
			new ShortOp("if_acmpeq", "offset"), 
			new ShortOp("if_acmpne", "offset"), 
			new ShortOp("goto", "offset"), 
			new Op("jsr"), 
			new Op("ret"), 
			new Op("tableswitch"), 
			new Op("lookupswitch"), 
			new Op("ireturn"), 
			new Op("lreturn"), 
			new Op("freturn"), 
			new Op("dreturn"), 
		} ,
		{
			new Op("areturn"), 
			new Op("return"), 
			new ShortOp("getstatic", "field"),
			new ShortOp("putstatic", "field"),
			new ShortOp("getfield", "field"),
			new ShortOp("putfield", "field"),
			new ReferenceOp("invokevirtual", "method"), 
			new ReferenceOp("invokespecial", "method"), 
			new ReferenceOp("invokestatic", "method"), 
			new ReferenceOp("invokeinterface", "method"), 
			new Op(""), 
			new ReferenceOp("new", "type"), 
			new Op("newarray"), 
			new ReferenceOp("anewarray", "type"),
			new Op("arraylength"), 
			new Op("athrow"), 
		} ,
		{
			new ReferenceOp("checkcast", "type"), 
			new ReferenceOp("instanceof", "type"), 
			new Op("monitorenter"), 
			new Op("monitorexit"), 
			new Op("wide"), 
			new Op("multianewarray"), 
			new ShortOp("ifnull", "offset"), 
			new ShortOp("ifnonnull", "offset"), 
			new Op("goto_w"), 
			new Op("jsr_w"), 
			new Op("breakpoint"), 
		},
		{
		},
		{
		},
		{
			new Op(""), 
			new Op(""), 
			new Op(""), 
			new Op(""), 
			new Op(""), 
			new Op(""), 
			new Op(""), 
			new Op(""), 
			new Op(""), 
			new Op(""), 
			new Op(""), 
			new Op(""), 
			new Op(""), 
			new Op(""), 
			new Op("impdep1"), 
			new Op("impdep2"),
		}
	};
}

class ClassFileDocument extends HtmlDocument {
	public ClassFileDocument(InputStream in, PrintStream out) throws IOException {
		super(in, out);
	}

	@Override
	protected void content() throws IOException {
		add(new HexString("magic number", 4));
		add(new ShortNode("minor version"));
		add(new ShortNode("major version"));

		short numConstants = add(new ShortNode("constant pool size"));
		Reference.pool = add(new ConstantPool(numConstants));

		add(new HexString("access flags", 2));
		add(new Reference("class"));
		add(new Reference("super"));

		short numInterfaces = add(new ShortNode("num interfaces"));
		add(new Interfaces(numInterfaces));

		short numFields = add(new ShortNode("num fields"));
		add(new Fields(numFields));

		short numMethods = add(new ShortNode("num methods"));
		add(new Methods(numMethods));
	}
}

public class decompile {

	public static void main(String args[]) throws IOException {
		
		InputStream in = System.in;
		
		if (args.length > 0) {
			in = new FileInputStream(args[0]);
		}
		
		new ClassFileDocument(in, System.out);

		/*

		int numMethods = read_short();
		System.out.printf("\nnum methods: %d\n", numMethods);

		for (int i = 0; i < numMethods; i++) {
			int access_flags = read_short();
			int name_index = read_short();
			int descriptor_index = read_short();
			System.out.printf("Method %s (%d) %s (%d)\n", constants.get(name_index), name_index, constants.get(descriptor_index), descriptor_index);

			int attributes_count = read_short();
			for (int j = 0; j < attributes_count; j++) {
				int id = read_short();
				len = read_int();
				byte[] value = read_bytes(len);
				System.out.printf("\t\tattribute: %s %s\n", constants.get(id), ""); //value);
			}
		}

		 */
	}

	public static long parseLong(byte[] bytes, int o) {
		return ((long)parseInteger(bytes, o) & 0xffffffffl) << 32 | (long)parseInteger(bytes, o + 4) & 0xffffffffl;
	}

	public static int parseInteger(byte[] bytes, int o) {
		return ((int)bytes[o] & 0xff) << 24 | ((int)bytes[o+1] & 0xff) << 16 | ((int)bytes[o+2] & 0xff) << 8 | ((int)bytes[o+3] & 0xff);
	}

	public static short parseShort(byte[] bytes, int o) {
		return (short)(((int)bytes[o] & 0xff) << 8 | (int)bytes[o+1] & 0xff);
	}

	static void print_hex(String tag, byte[] bytes) {
		System.out.print(tag);
		for (byte b: bytes) {
			System.out.printf("%x", b);
		}
		System.out.println();
	}

	static void print_chars(String tag, byte[] bytes) {
		System.out.print(tag);
		for (byte b: bytes) {
			System.out.print((char)b);
		}
		System.out.println();
	}

	static void print_int(String tag, int val) {
		System.out.printf("%s%d\n", tag, val);
	}
}
