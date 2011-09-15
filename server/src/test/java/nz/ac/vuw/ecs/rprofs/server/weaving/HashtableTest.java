package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.data.TestClazzCreator;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import org.junit.Before;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 15/09/11
 */
public class HashtableTest extends WeaverTestBase {

	ClazzId id;
	Clazz clazz;
	ClassRecord record;
	byte[] input;

	@Before
	public void setUp() throws Exception {
		id = new ClazzId(1l);
		input = new byte[0];
		TestClazzCreator c = new TestClazzCreator(id);
		new ClassParser(c).read(input);
		clazz = c.get();
		record = new ClassRecord(clazz);
		record.addFields(c.fields);
		record.addMethods(c.methods);
	}
}
