/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.reports;

import nz.ac.vuw.ecs.rprofs.client.data.Report.ClassEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.InstanceEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.PackageEntry;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;


/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class InstancesReport<T, C extends InstancesReport<?, ?>> extends AbstractReport<T, C> {

	int classes;
	int instances;
	int reads;
	int writes;
	int ereads;
	int ewrites;

	protected InstancesReport() {}

	private InstancesReport(InstancesReport<T, C> o) {
		this.classes = o.classes;
		this.instances = o.instances;
		this.reads = o.reads;
		this.writes = o.writes;
		this.ereads = o.ereads;
		this.ewrites = o.ewrites;
	}

	protected InstancesReport<T, C> toRPC() {
		InstancesReport<T, C> result = new InstancesReport<T, C>(this);
		for (InstancesReport<?, ?> child: getChildren()) {
			InstancesReport<?, ?> c = child.toRPC();
			result.classes += c.classes;
			result.instances += c.instances;
			result.reads += c.reads;
			result.writes += c.writes;
			result.ereads += c.ereads;
			result.ewrites += c.ewrites;
		}
		return result;
	}
	
	protected int[] getValues() {
		return new int[] { reads, writes, ereads, ewrites };
	}
	
	public static PackageReport create(String target) {
		PackageReport report = new PackageReport();
		report.setTarget(target);
		return report;
	}
	
	public static class PackageReport extends InstancesReport<String, ClassReport> {
		public PackageEntry toEntry() {
			InstancesReport<String, ClassReport> rpc = toRPC();
			return new PackageEntry(getTarget(), rpc.classes, rpc.instances, rpc.getValues());
		}
	}

	public static ClassReport create(Class target) {
		ClassReport report = new ClassReport();
		report.setTarget(target);
		return report;
	}
	
	public static class ClassReport extends InstancesReport<Class, InstanceReport> {
		public ClassEntry toEntry() {
			InstancesReport<Class, InstanceReport> rpc = toRPC();
			return new ClassEntry(getTarget().toRPC(), rpc.instances, rpc.getValues());
		}
	}
	
	public static InstanceReport create(Long target) {
		InstanceReport report = new InstanceReport();
		report.setTarget(target);
		return report;
	}
	
	public static class InstanceReport extends InstancesReport<Long, InstancesReport<?, ?>> {
		public InstanceEntry toEntry() {
			InstancesReport<Long, InstancesReport<?, ?>> rpc = toRPC();
			return new InstanceEntry(getTarget(), rpc.getValues());
		}
	}
}
