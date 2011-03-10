package nz.ac.vuw.ecs.rprofs.server.reports;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.data.Context;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.FieldWriteRecord;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;

public class FinalFieldReport extends Event.AbstractVisitor implements InstanceReport {

	public static final class ReportFactory implements InstanceReportFactory<FinalFieldReport> {

		private final Context context;

		public ReportFactory(Context context) {
			this.context = context;
		}

		@Override
		public FinalFieldReport generateReport(Instance instance) {
			return new FinalFieldReport(instance);
		}

		@Override
		public void processResults(FinalFieldReport report) {
			context.storeReports(report.getResults());
		}

	}

	private final Instance instance;

	private Set<Field> constructorWrites;
	private Set<Field> writes;

	public FinalFieldReport(Instance instance) {
		this.instance = instance;

		constructorWrites = Collections.newSet();
		writes = Collections.newSet();
	}

	public void run() {
		for (Event e: instance.getEvents()) {
			e.visit(this);
		}
	}

	public Collection<FieldWriteRecord> getResults() {
		List<FieldWriteRecord> results = Collections.newList();
		for (Field f: constructorWrites) {
			results.add(new FieldWriteRecord(instance, f, FieldWriteRecord.CONSTRUCTOR_PHASE));
		}
		for (Field f: writes) {
			results.add(new FieldWriteRecord(instance, f, FieldWriteRecord.POST_CONSTRUCTOR_PHASE));
		}
		return results;
	}

	@Override
	public void visitMethodReturn(Event e) {
		Method m = e.getMethod();
		if (m.isInit()) {
			constructorWrites.addAll(writes);
			writes.clear();
		}
	}

	@Override
	public void visitFieldWrite(Event e) {
		writes.add(e.getField());
	}
}
