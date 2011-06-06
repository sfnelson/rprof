package nz.ac.vuw.ecs.rprofs.server.reports;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import nz.ac.vuw.ecs.rprofs.server.context.Context;
import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.data.ClassManager;
import nz.ac.vuw.ecs.rprofs.server.data.InstanceManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Argument;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.request.ClassService;
import nz.ac.vuw.ecs.rprofs.server.request.InstanceService;


public class DatasetReport {

	private ContextManager cm = ContextManager.getInstance();
	private ClassService classes = new ClassManager();
	private InstanceService instances = new InstanceManager();

	public int getNumClasses() {
		return classes.findNumClasses();
	}

	public int getNumObjects() {
		return instances.findNumInstances();
	}

	public Stat getObjectsPerClass() {
		Context c = cm.getCurrent();

		CriteriaBuilder builder = c.em().getCriteriaBuilder();

		CriteriaQuery<Long> query = builder.createQuery(Long.TYPE);
		Root<Instance> instance = query.from(Instance.class);
		Path<nz.ac.vuw.ecs.rprofs.server.domain.Class> type = instance.get("type");

		query.select(builder.count(instance));
		query.where(builder.isNotNull(type));
		query.groupBy(type);

		return ReportManager.computeStats(c.em().createQuery(query).getResultList());
	}

	public Stat getWritesPerClass() {
		Context c = cm.getCurrent();

		CriteriaBuilder builder = c.em().getCriteriaBuilder();

		CriteriaQuery<Long> query = builder.createQuery(Long.TYPE);
		Root<Event> event = query.from(Event.class);
		Path<nz.ac.vuw.ecs.rprofs.server.domain.Class> type = event.get("type");
		Path<Integer> eventType = event.get("event");

		query.select(builder.count(event));
		query.where(builder.and(builder.isNotNull(type), builder.equal(eventType, Event.FIELD_WRITE)));
		query.groupBy(type);

		return ReportManager.computeStats(c.em().createQuery(query).getResultList());
	}

	public Stat getWritesPerObject() {
		Context c = cm.getCurrent();

		CriteriaBuilder builder = c.em().getCriteriaBuilder();

		CriteriaQuery<Long> query = builder.createQuery(Long.TYPE);
		Root<Event> event = query.from(Event.class);
		Join<Event, Argument> args = event.join("args");
		Path<Integer> position = args.get("position");
		Path<Instance> parameter = args.get("parameter");
		Path<Integer> eventType = event.get("event");

		query.select(builder.count(event));
		query.where(builder.and(builder.equal(eventType, Event.FIELD_WRITE), builder.equal(position, 0)));
		query.groupBy(parameter);

		return ReportManager.computeStats(c.em().createQuery(query).getResultList());
	}

	public Stat getReadsPerClass() {
		Context c = cm.getCurrent();

		CriteriaBuilder builder = c.em().getCriteriaBuilder();

		CriteriaQuery<Long> query = builder.createQuery(Long.TYPE);
		Root<Event> event = query.from(Event.class);
		Path<nz.ac.vuw.ecs.rprofs.server.domain.Class> type = event.get("type");
		Path<Integer> eventType = event.get("event");

		query.select(builder.count(event));
		query.where(builder.and(builder.isNotNull(type), builder.equal(eventType, Event.FIELD_READ)));
		query.groupBy(type);

		return ReportManager.computeStats(c.em().createQuery(query).getResultList());
	}

	public Stat getReadsPerObject() {
		Context c = cm.getCurrent();

		CriteriaBuilder builder = c.em().getCriteriaBuilder();

		CriteriaQuery<Long> query = builder.createQuery(Long.TYPE);
		Root<Event> event = query.from(Event.class);
		Join<Event, Argument> args = event.join("args");
		Path<Integer> position = args.get("position");
		Path<Instance> parameter = args.get("parameter");
		Path<Integer> eventType = event.get("event");

		query.select(builder.count(event));
		query.where(builder.and(builder.equal(eventType, Event.FIELD_READ), builder.equal(position, 0)));
		query.groupBy(parameter);

		return ReportManager.computeStats(c.em().createQuery(query).getResultList());
	}
}
