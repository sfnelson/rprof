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
public class WritesReport<T, C extends WritesReport<?, ?>> extends AbstractReport<T, C> {

	int classes;
	int instances;
	int writes;
	int constructor;
	int read;
	int equals;
	
	boolean fieldRead;
	boolean equalsCalled;

	protected WritesReport() {}

	private WritesReport(WritesReport<T, C> o) {
		this.classes = o.classes;
		this.instances = o.instances;
		this.writes = o.writes;
		this.constructor = o.constructor;
		this.read = o.read;
		this.equals = o.equals;
	}

	protected WritesReport<T, C> toRPC() {
		WritesReport<T, C> result = new WritesReport<T, C>(this);
		for (WritesReport<?, ?> child: getChildren()) {
			WritesReport<?, ?> c = child.toRPC();
			result.classes += c.classes;
			result.instances += c.instances;
			result.writes += c.writes;
			result.constructor += c.constructor;
			result.read += c.read;
			result.equals += c.equals;
		}
		return result;
	}
	
	protected int[] getValues() {
		return new int[] { writes, constructor, read, equals };
	}
	
	public static PackageReport create(String target) {
		PackageReport report = new PackageReport();
		report.setTarget(target);
		return report;
	}
	
	public static class PackageReport extends WritesReport<String, ClassReport> {
		public PackageEntry toEntry() {
			WritesReport<String, ClassReport> rpc = toRPC();
			return new PackageEntry(getTarget(), rpc.classes, rpc.instances, rpc.getValues());
		}
	}

	public static ClassReport create(Class target) {
		ClassReport report = new ClassReport();
		report.setTarget(target);
		return report;
	}
	
	public static class ClassReport extends WritesReport<Class, InstanceReport> {
		public ClassEntry toEntry() {
			WritesReport<Class, InstanceReport> rpc = toRPC();
			return new ClassEntry(getTarget().toRPC(), rpc.instances, rpc.getValues());
		}
	}
	
	public static InstanceReport create(Long target) {
		InstanceReport report = new InstanceReport();
		report.setTarget(target);
		return report;
	}
	
	public static class InstanceReport extends WritesReport<Long, WritesReport<?, ?>> {
		public InstanceEntry toEntry() {
			WritesReport<Long, WritesReport<?, ?>> rpc = toRPC();
			return new InstanceEntry(getTarget(), rpc.getValues());
		}
	}
}
