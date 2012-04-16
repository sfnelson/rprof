package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.data.util.ClazzCreator;
import nz.ac.vuw.ecs.rprofs.server.data.util.FieldCreator;
import nz.ac.vuw.ecs.rprofs.server.data.util.MethodCreator;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

import static org.easymock.EasyMock.*;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 13/09/11
 */
public class ClassParserTest {

	ClazzCreator builder;
	MethodCreator mbuilder;
	FieldCreator fbuilder;

	ClassParser parser;

	@Before
	public void setUp() throws Exception {
		builder = createMock(ClazzCreator.class);
		mbuilder = createMock(MethodCreator.class);
		fbuilder = createMock(FieldCreator.class);

		parser = new ClassParser(builder);
	}

	@Test
	public void testVisit() throws Exception {

		expect(builder.setName("org.foo.Bar")).andReturn(builder);
		expect(builder.setParentName("org.Foo")).andReturn(builder);
		expect(builder.setAccess(Opcodes.ACC_PUBLIC)).andReturn(builder);

		replay(builder, mbuilder, fbuilder);

		parser.visit(50, Opcodes.ACC_PUBLIC, "org.foo.Bar", null, "org.Foo", null);

		verify(builder, mbuilder, fbuilder);
	}

	@Test
	public void testVisitSource() throws Exception {

		replay(builder, mbuilder, fbuilder);

		parser.visitSource(null, null);

		verify(builder, mbuilder, fbuilder);
	}

	@Test
	public void testVisitOuterClass() throws Exception {

		replay(builder, mbuilder, fbuilder);

		parser.visitOuterClass(null, null, null);

		verify(builder, mbuilder, fbuilder);
	}

	@Test
	public void testVisitAnnotation() throws Exception {

		replay(builder, mbuilder, fbuilder);

		parser.visitAnnotation(null, true);

		verify(builder, mbuilder, fbuilder);
	}

	@Test
	public void testVisitAttribute() throws Exception {

		replay(builder, mbuilder, fbuilder);

		parser.visitAttribute(null);

		verify(builder, mbuilder, fbuilder);
	}

	@Test
	public void testVisitInnerClass() throws Exception {

		replay(builder, mbuilder, fbuilder);

		parser.visitInnerClass(null, null, null, 0);

		verify(builder, mbuilder, fbuilder);
	}

	@Test
	public void testVisitField() throws Exception {

		expect(builder.addField()).andReturn(fbuilder);
		expect(fbuilder.setAccess(15)).andReturn(fbuilder);
		expect(fbuilder.setName("baz")).andReturn(fbuilder);
		expect(fbuilder.setDescription("I")).andReturn(fbuilder);
		expect(fbuilder.store()).andReturn(null);

		replay(builder, mbuilder, fbuilder);

		parser.visitField(15, "baz", "I", null, null);

		verify(builder, mbuilder, fbuilder);
	}

	@Test
	public void testVisitMethod() throws Exception {

		expect(builder.addMethod()).andReturn(mbuilder);
		expect(mbuilder.setAccess(15)).andReturn(mbuilder);
		expect(mbuilder.setName("foobar")).andReturn(mbuilder);
		expect(mbuilder.setDescription("(Lorg.foo.Bar;I)I")).andReturn(mbuilder);
		expect(mbuilder.store()).andReturn(null);

		replay(builder, mbuilder, fbuilder);

		parser.visitMethod(15, "foobar", "(Lorg.foo.Bar;I)I", null, null);

		verify(builder, mbuilder, fbuilder);
	}

	@Test
	public void testVisitEnd() throws Exception {

		replay(builder, mbuilder, fbuilder);

		parser.visitEnd();

		verify(builder, mbuilder, fbuilder);
	}
}
