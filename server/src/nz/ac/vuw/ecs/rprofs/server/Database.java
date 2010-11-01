package nz.ac.vuw.ecs.rprofs.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.data.FieldRecord;
import nz.ac.vuw.ecs.rprofs.server.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.server.data.MethodRecord;
import nz.ac.vuw.ecs.rprofs.server.data.ProfilerRun;
import nz.ac.vuw.ecs.rprofs.server.data.Template;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.gwt.dev.util.collect.Lists;


public class Database implements InitializingBean, ProfilerDataSource<ClassRecord, MethodRecord, FieldRecord, ProfilerRun, LogRecord> {

	private DataSource db;
	private JdbcTemplate template;

	private final Template<ProfilerRun, Void> prt = ProfilerRun.getTemplate();
	private final Template<ClassRecord, nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun> crt
	= ClassRecord.getTemplate();
	private final Template<MethodRecord, nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun> mrt
	= MethodRecord.getTemplate();
	private final Template<FieldRecord, nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun> frt
	= FieldRecord.getTemplate();
	private final Template<LogRecord, nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun> lrt
	= LogRecord.getTemplate();

	public void setDataSource(DataSource db) {
		this.db = db;
	}

	public void afterPropertiesSet() throws Exception {
		template = new JdbcTemplate(db);

		//template.update(prt.createTable(null));
	}

	public void storeClasses(ProfilerRun run, List<ClassRecord> classes) {
		template.batchUpdate(crt.insert(run), crt.inserter(classes));
		for (ClassRecord cr: classes) {
			template.batchUpdate(mrt.insert(run), mrt.inserter(cr.getMethods()));
			template.batchUpdate(frt.insert(run), frt.inserter(cr.getFields()));
		}
	}

	public int getNumClasses(nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun run) {
		return template.queryForInt(crt.countSelect(run));
	}

	@Override
	public List<ClassRecord> getClasses(nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun run) {
		List<ClassRecord> classes = template.query(crt.select(run), crt.mapper(null));

		final Map<Integer, ClassRecord> classMap = new HashMap<Integer, ClassRecord>();
		for (ClassRecord cr: classes) {
			classMap.put(cr.id, cr);
		}

		template.query(mrt.select(run), mrt.mapper(classMap));
		template.query(frt.select(run), frt.mapper(classMap));

		return classes;
	}

	@Override
	public List<ProfilerRun> getProfiles() {
		return template.query(prt.select(null), prt.mapper(null));
	}

	public ProfilerRun createRun() {

		ProfilerRun run = new ProfilerRun();

		template.batchUpdate(prt.insert(null), prt.inserter(Lists.create(run)));
		template.update(crt.createTable(run));
		template.update(mrt.createTable(run));
		template.update(frt.createTable(run));
		template.update(lrt.createTable(run));

		return run;
	}

	public void update(ProfilerRun run) {
		template.batchUpdate(prt.update(null), prt.updater(Lists.create(run)));
	}

	@Override
	public void dropRun(nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun run) {
		template.batchUpdate(prt.delete(null), prt.deleter(Lists.create(new ProfilerRun(run))));
		drop(crt.drop(run));
		drop(mrt.drop(run));
		drop(frt.drop(run));
		drop(lrt.drop(run));
	}

	private void drop(String table) {
		try {
			template.update(table);
		} catch (Exception ex) {
			System.err.println("Unable to drop table: " + ex.getMessage() + "\n\t(" + table + ")");
		}
	}
	
	@Override
	public List<LogRecord> getLogs(nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun run, int offset, int limit, int flags, int cls) {
		return template.query(lrt.select(run, offset, limit, flags, cls), lrt.mapper(null));
	}

	@Override
	public int getNumLogs(nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun run,
			int flags, int cls) {
		return template.queryForInt(lrt.countSelect(run, flags, cls));
	}

	public void storeLogs(ProfilerRun run, List<LogRecord> records) {
		template.batchUpdate(lrt.insert(run), lrt.inserter(records));
	}

	public void update(ProfilerRun run, List<LogRecord> records) {
		template.batchUpdate(lrt.update(run), lrt.updater(records));
	}

	public void deleteLogs(ProfilerRun run, List<LogRecord> toDelete) {
		template.batchUpdate(lrt.delete(run), lrt.deleter(toDelete));
	}
}
