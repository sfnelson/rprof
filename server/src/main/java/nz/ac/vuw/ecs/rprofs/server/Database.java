package nz.ac.vuw.ecs.rprofs.server;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import nz.ac.vuw.ecs.rprofs.client.data.ClassInfo;
import nz.ac.vuw.ecs.rprofs.client.data.InstanceInfo;
import nz.ac.vuw.ecs.rprofs.client.data.RunInfo;
import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.server.data.RunRecord;



public class Database {

	@PersistenceContext
	private EntityManager em;

	public Collection<RunRecord> getProfiles() {
		return em.createQuery("select R from RunRecord R", RunRecord.class).getResultList();
	}

	public RunRecord getRun(RunInfo run) {
		TypedQuery<RunRecord> q = em.createQuery("select R from RunRecord R where R.handle = ?", RunRecord.class);
		q.setParameter(1, run.getHandle());
		return q.getSingleResult();
	}

	public List<ClassRecord> getClasses() {
		return em.createQuery("select C from ClassRecord C", ClassRecord.class).getResultList();
	}

	public int getNumLogs(int type) {
		Query q = em.createQuery("select count(*) from LogRecord L where band(L.event, ?) <> 0");
		q.setParameter(1, type);
		return ((Long)q.getSingleResult()).intValue();
	}

	public int getNumLogs(int type, InstanceInfo instance) {
		Query q = em.createQuery("select L from LogRecord L where band(L.event, ?) <> 0 and L.args[0] = ?");
		q.setParameter(1, type);
		q.setParameter(2, instance.getId());
		return ((Long)q.getSingleResult()).intValue();
	}

	public int getNumLogs(int type, ClassInfo cls) {
		Query q = em.createQuery("select L from LogRecord L where band(L.event, ?) <> 0 and L.cnum = ?");
		q.setParameter(1, type);
		q.setParameter(2, cls.getId());
		return ((Long)q.getSingleResult()).intValue();
	}

	public List<LogRecord> getLogs(int offset, int limit, int type) {
		TypedQuery<LogRecord> q = em.createQuery("select L from LogRecord L where band(L.event, ?) <> 0", LogRecord.class);
		q.setParameter(1, type);
		return q.getResultList();
	}

	public List<LogRecord> getLogs(int offset, int limit, int type, InstanceInfo instance) {
		TypedQuery<LogRecord> q = em.createQuery("select L from LogRecord L where band(L.event, ?) <> 0 and L.args[0] = ?", LogRecord.class);
		q.setParameter(1, type);
		q.setParameter(2, instance.getId());
		return q.getResultList();
	}

	public List<LogRecord> getLogs(int offset, int limit, int type, ClassInfo cls) {
		TypedQuery<LogRecord> q = em.createQuery("select L from LogRecord L where band(L.event, ?) <> 0 and L.cnum = ?", LogRecord.class);
		q.setParameter(1, type);
		q.setParameter(2, cls.getId());
		return q.getResultList();
	}

	public RunRecord createRun() {
		RunRecord run = RunRecord.create();
		em.merge(run);
		return run;
	}

	public void updateRun(RunRecord run) {
		em.merge(run);
	}

	public void deleteRun(RunRecord run) {
		em.remove(run);
	}

	public void saveClasses(RunInfo run, List<ClassRecord> classes) {
		for (ClassRecord c: classes) {
			em.merge(c);
		}
	}

	public void saveLogs(RunInfo run, List<LogRecord> records) {
		for (LogRecord r: records) {
			em.merge(r);
		}
	}

	public void close() {
		em.close();
	}
}
