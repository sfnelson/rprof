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

	public void storeClasses(ProfilerRun run, Iterable<ClassRecord> classes) {
		for (ClassRecord cr: classes) {
			template.update(crt.insert(run), crt.inserter(cr));
			for (MethodRecord mr: cr.getMethods()) {
				template.update(mrt.insert(run), mrt.inserter(mr));
			}
			for (FieldRecord fr: cr.getFields()) {
				template.update(frt.insert(run), frt.inserter(fr));
			}
		}
	}

	public List<ClassRecord> getClasses(nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun run) {
		List<ClassRecord> classes = template.query(crt.selectAll(run), crt.mapper(null));

		final Map<Integer, ClassRecord> classMap = new HashMap<Integer, ClassRecord>();
		for (ClassRecord cr: classes) {
			classMap.put(cr.id, cr);
		}

		template.query(mrt.selectAll(run), mrt.mapper(classMap));
		template.query(frt.selectAll(run), mrt.mapper(classMap));

		return classes;
	}

	public List<ProfilerRun> getProfiles() {
		return template.query(prt.selectAll(null), prt.mapper(null));
	}

	public ProfilerRun createRun() {
		
		ProfilerRun run = new ProfilerRun();

		template.update(prt.insert(null), prt.inserter(run));
		template.update(crt.createTable(run));
		template.update(mrt.createTable(run));
		template.update(frt.createTable(run));
		template.update(lrt.createTable(run));

		return run;
	}

	public void update(ProfilerRun run) {
		template.update(prt.update(null), prt.updater(run));
	}

	public void dropRun(nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun run) {
		template.update(prt.delete(null), prt.deleter(new ProfilerRun(run)));
		template.update(crt.drop(run));
		template.update(mrt.drop(run));
		template.update(frt.drop(run));
		template.update(lrt.drop(run));
	}

	public List<LogRecord> getLogs(nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun run) {
		return template.query(lrt.selectAll(run), lrt.mapper(null));
	}

	public List<LogRecord> getLogs(nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun run, int offset, int limit) {
		return template.query(lrt.select(run, offset, limit), lrt.mapper(null));
	}

	public int getNumLogs(nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun run) {
		return template.queryForInt(lrt.countSelectAll(run));
	}

	public void storeLogs(ProfilerRun run, List<LogRecord> records) {
		for (LogRecord record: records) {
			template.update(lrt.insert(run), lrt.inserter(record));
		}
	}

	public void update(ProfilerRun run, LogRecord record) {
		template.update(lrt.update(run), lrt.updater(record));
	}

	public void deleteLogs(ProfilerRun run, List<LogRecord> toDelete) {
		for (LogRecord r: toDelete) {
			template.update(lrt.delete(run), lrt.deleter(r));
		}
	}
}
