/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.reports;

import nz.ac.vuw.ecs.rprofs.client.data.Report.ClassEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.PackageEntry;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ProblemReport<T, C extends ProblemReport<?, ?>> extends AbstractReport<T, C> {
	
	int classes;
	int weave;
	int init;
	int instances;
	int flags;
	
	protected ProblemReport() {}
	
	private ProblemReport(ProblemReport<T, C> o) {
		this.classes = o.classes;
		this.weave = o.weave;
		this.init = o.init;
		this.instances = o.instances;
		this.flags = o.flags;
	}
	
	protected ProblemReport<T, C> toRPC() {
		ProblemReport<T, C> result = new ProblemReport<T, C>(this);
		for (ProblemReport<?, ?> child: getChildren()) {
			ProblemReport<?, ?> c = child.toRPC();
			result.classes += c.classes;
			result.init += c.init;
			result.weave += c.weave;
			result.instances += c.instances;
			result.flags |= flags;
		}
		return result;
	}
	
	protected int[] getValues() {
		return new int[] { weave, init, instances, flags };
	}
	
	public static PackageReport create(String target) {
		PackageReport report = new PackageReport();
		report.setTarget(target);
		return report;
	}
	
	public static class PackageReport extends ProblemReport<String, ClassReport> {
		public PackageEntry toEntry() {
			ProblemReport<String, ClassReport> rpc = toRPC();
			return new PackageEntry(getTarget(), rpc.classes, 0, rpc.getValues());
		}
	}
	
	public static ClassReport create(Class cr) {
		ClassReport report = new ClassReport();
		report.setTarget(cr);
		return report;
	}
	
	public static class ClassReport extends ProblemReport<Class, ProblemReport<Void, ?>> {
		public ClassEntry toEntry() {
			ProblemReport<Class, ProblemReport<Void, ?>> rpc = toRPC();
			return new ClassEntry(getTarget().toRPC(), 0, rpc.getValues());
		}
	}
}
