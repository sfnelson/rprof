package nz.ac.vuw.ecs.rprofs.server.data;

import com.google.common.collect.Lists;
import nz.ac.vuw.ecs.rprofs.server.data.util.*;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;
import org.junit.Test;

import java.util.Date;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 10/09/11
 */
public class ClassManagerTest {

	ClassManager cm;
	Dataset dataset;
	Database database;
	ClazzCreator creator;
	ClazzUpdater updater;
	ClazzQuery query;
	FieldQuery fQuery;
	MethodQuery mQuery;
	Query.Cursor cursor;

	@org.junit.Before
	public void setup() {
		database = createMock(Database.class);
		creator = createMock(ClazzCreator.class);
		updater = createMock(ClazzUpdater.class);
		query = createMock(ClazzQuery.class);
		fQuery = createMock(FieldQuery.class);
		mQuery = createMock(MethodQuery.class);
		cursor = createMock(Query.Cursor.class);

		dataset = new Dataset(new DatasetId(1l), "foo", new Date());

		cm = new ClassManager();
		cm.database = database;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateClazz() {

		expect(database.getClazzCreator()).andReturn(creator);

		replay(database, creator);

		ClazzCreator c = cm.createClazz();

		verify(database, creator);
	}

	@Test
	public void testGetClazzById() {
		ClazzId id = new ClazzId(1l);
		Clazz clazz = new Clazz(id, "foo", null, null, 0);

		expect(database.findEntity(id)).andReturn(clazz);

		replay(database);

		Clazz returned = cm.getClazz(id);

		verify(database);

		assertSame(returned, clazz);
	}

	@Test
	public void testGetClazzByIdNotFound() {
		ClazzId id = new ClazzId(1l);

		expect(database.findEntity(id)).andReturn(null);

		replay(database);

		Clazz returned = cm.getClazz(id);

		verify(database);

		assertNull(returned);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetClazzByName() {
		ClazzId id = new ClazzId(1l);
		Clazz clazz = new Clazz(id, "foo", null, null, 0);

		expect(database.getClazzQuery()).andReturn(query);
		expect(query.setName("foo")).andReturn(query);
		expect(query.find()).andReturn(cursor);
		expect(cursor.hasNext()).andReturn(true);
		expect(cursor.next()).andReturn(clazz);
		cursor.close();

		replay(database, query, cursor);

		Clazz returned = cm.getClazz("foo");

		verify(database, query, cursor);

		assertSame(returned, clazz);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetClazzByNameNotFound() {
		ClazzId id = new ClazzId(1l);

		expect(database.getClazzQuery()).andReturn(query);
		expect(query.setName("foo")).andReturn(query);
		expect(query.find()).andReturn(cursor);
		expect(cursor.hasNext()).andReturn(false);
		cursor.close();

		replay(database, query, cursor);

		Clazz returned = cm.getClazz("foo");

		verify(database, query, cursor);

		assertNull(returned);
	}

	@Test
	public void testFindPackages() {
		expect(database.findPackages()).andReturn(Lists.<String>newArrayList());

		replay(database);

		cm.findPackages();

		verify(database);
	}

	@Test
	public void testFindNumPackages() {
		expect(database.countPackages()).andReturn(15l);

		replay(database);

		cm.findNumPackages();

		verify(database);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindClasses() {
		expect(database.getClazzQuery()).andReturn(query);
		expect(query.find()).andReturn(cursor);
		expect(cursor.hasNext()).andReturn(false);
		cursor.close();

		replay(database, query, cursor);

		assertEquals(Lists.newArrayList(), cm.findClasses());

		verify(database, query, cursor);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindClassesInPackage() {
		expect(database.getClazzQuery()).andReturn(query);
		expect(query.setPackageName("foobar")).andReturn(query);
		expect(query.find()).andReturn(cursor);
		expect(cursor.hasNext()).andReturn(false);
		cursor.close();

		replay(database, query, cursor);

		assertEquals(Lists.newArrayList(), cm.findClasses("foobar"));

		verify(database, query, cursor);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindNumClasses() {
		expect(database.getClazzQuery()).andReturn(query);
		expect(query.count()).andReturn(15l);

		replay(database, query);

		cm.findNumClasses();

		verify(database, query);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindNumClassesInPackage() {
		expect(database.getClazzQuery()).andReturn(query);
		expect(query.setPackageName("foobar")).andReturn(query);
		expect(query.count()).andReturn(15l);

		replay(database, query);

		cm.findNumClasses("foobar");

		verify(database, query);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindFields() {
		ClazzId id = new ClazzId(1l);

		expect(database.getFieldQuery()).andReturn(fQuery);
		expect(fQuery.setOwner(id)).andReturn(fQuery);
		expect(fQuery.find()).andReturn(cursor);
		expect(cursor.hasNext()).andReturn(false);
		cursor.close();

		replay(database, fQuery, cursor);

		assertEquals(Lists.newArrayList(), cm.findFields(id));

		verify(database, fQuery, cursor);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindMethods() {
		ClazzId id = new ClazzId(1l);

		expect(database.getMethodQuery()).andReturn(mQuery);
		expect(mQuery.setOwner(id)).andReturn(mQuery);
		expect(mQuery.find()).andReturn(cursor);
		expect(cursor.hasNext()).andReturn(false);
		cursor.close();

		replay(database, mQuery, cursor);

		assertEquals(Lists.newArrayList(), cm.findMethods(id));

		verify(database, mQuery, cursor);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSetProperties() {
		ClazzId id = new ClazzId(1l);

		expect(database.getClazzUpdater()).andReturn(updater);
		expect(updater.setProperties(15)).andReturn(updater);
		updater.update(id);

		replay(database, updater);

		cm.setProperties(id, 15);

		verify(database, updater);
	}
}
