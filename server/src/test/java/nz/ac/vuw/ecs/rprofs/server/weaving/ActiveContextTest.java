package nz.ac.vuw.ecs.rprofs.server.weaving;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import javax.persistence.EntityManager;

import nz.ac.vuw.ecs.rprofs.server.context.Context;
import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.data.ClassManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

public class ActiveContextTest extends EasyMockSupport {

	private ActiveContext context;
	private ContextManager mockManager;
	private Context mockDefault;
	private Context mockContext;
	private Dataset mockDataset;
	private ClassManager mockClasses;
	private EntityManager em;

	@Before
	public void setUp() throws Exception {
		mockManager = createMock(ContextManager.class);
		mockDefault = createMock(Context.class);
		mockContext = createMock(Context.class);
		mockDataset = createMock(Dataset.class);
		mockClasses = createMock(ClassManager.class);
		em = createMock(EntityManager.class);
		context = new ActiveContext(mockManager, mockContext, mockDataset);
	}

	@Test
	public void testGetDataset() {
		replayAll();
		assertEquals(context.getDataset(), mockDataset);
		verifyAll();
	}

	@Test
	public void testGetContext() {
		replayAll();
		assertEquals(context.getContext(), mockContext);
		verifyAll();
	}

	@Test
	public void testSetMainMethod() {
		final String PROGRAM = "main method";

		// record
		expect(mockManager.getDefault()).andReturn(mockDefault);
		mockDefault.open();
		expect(mockDataset.getId()).andReturn((short) 15);
		expect(mockDefault.find(Dataset.class, (short) 15)).andReturn(mockDataset);
		mockDataset.setProgram(PROGRAM);
		mockDefault.close();

		replayAll();

		// test
		context.setMainMethod(PROGRAM);

		verifyAll();
	}

	@Test
	public void testStoreClass() {

		ClassRecord root = new ClassRecord(null, new ClassId((short) 15, 1));
		root.init(0, 0, "org.Root", "", null, null);

		ClassRecord foo = new ClassRecord(null, new ClassId((short) 15, 2));
		foo.init(0, 0, "org.Foo", "", "org.Root", null);

		ClassRecord bar = new ClassRecord(null, new ClassId((short) 15, 3));
		bar.init(0, 0, "org.Bar", "", "org.Foo", null);

		// store root
		expect(mockContext.em()).andReturn(em);
		em.persist(EasyMock.eq(root.toClass()));
		expect(mockContext.em()).andReturn(em);
		expect(mockDataset.getId()).andReturn((short) 15);
		em.persist(EasyMock.anyObject(Event.class));

		replayAll();

		context.storeClass(root);

		verifyAll();

		// store bar
		resetAll();

		expect(mockContext.em()).andReturn(em);
		em.persist(EasyMock.eq(bar.toClass()));
		expect(mockManager.getClasses()).andReturn(mockClasses);
		expect(mockClasses.findClass("org.Foo")).andReturn(null);
		expect(mockContext.em()).andReturn(em);
		expect(mockDataset.getId()).andReturn((short) 15);
		em.persist(EasyMock.anyObject(Event.class));

		replayAll();

		context.storeClass(bar); // store bar

		// store foo
		resetAll();

		Class barCls = bar.toClass();
		Capture<Class> fooCapture = new Capture<Class>();

		expect(mockContext.em()).andReturn(em);
		em.persist(EasyMock.capture(fooCapture));
		expect(mockManager.getClasses()).andReturn(mockClasses);
		expect(mockClasses.findClass("org.Root")).andReturn(root.toClass());
		expect(mockContext.em()).andReturn(em);
		expect(em.find(Class.class, bar.id)).andReturn(barCls);
		expect(mockContext.em()).andReturn(em);
		expect(mockDataset.getId()).andReturn((short) 15);
		em.persist(EasyMock.anyObject(Event.class));

		replayAll();

		context.storeClass(foo); // store foo
		assertEquals(root.toClass(), fooCapture.getValue().getParent());
		assertEquals(foo.toClass(), barCls.getParent());

		verifyAll();
	}

}
