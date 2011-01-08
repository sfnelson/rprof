/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.reports;

import nz.ac.vuw.ecs.rprofs.client.data.Report.ClassEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.InstanceEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.PackageEntry;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ClassesReport<Target, Child extends ClassesReport<?, ?>> extends AbstractReport<Target, Child> {
	
	int classes;
	int methods;
	int flags;
	int equals;
	int hash;
	
	protected ClassesReport() {}
	
	private ClassesReport(ClassesReport<Target, Child> o) {
		setTarget(o.getTarget());
		this.classes = o.classes;
		this.methods = o.methods;
		this.flags = o.flags;
		this.equals = o.equals;
		this.hash = o.hash;
	}
	
	protected ClassesReport<Target, Child> toRPC() {
		ClassesReport<Target, Child> rpc = new ClassesReport<Target, Child>(this);
		for (Child child: getChildren()) {
			ClassesReport<?, ?> c = child.toRPC();
			rpc.classes += c.classes;
			rpc.methods += c.methods;
			rpc.flags |= c.flags;
			rpc.equals += c.equals;
			rpc.hash += c.hash;
		}
		return rpc;
	}
	
	protected int[] getValues() {
		return new int[] { flags, equals, hash };
	}
	
	public static PackageReport create(String target) {
		PackageReport report = new PackageReport();
		report.setTarget(target);
		return report;
	}
	
	public static class PackageReport extends ClassesReport<String, ClassReport> {
		public PackageEntry toEntry() {
			ClassesReport<String, ClassReport> rpc = toRPC();
			return new PackageEntry(getTarget(), rpc.classes, rpc.methods, rpc.getValues());
		}
	}

	public static ClassReport create(Class target) {
		ClassReport report = new ClassReport();
		report.setTarget(target);
		return report;
	}
	
	public static class ClassReport extends ClassesReport<Class, MethodReport> {
		public ClassEntry toEntry() {
			ClassesReport<Class, MethodReport> rpc = toRPC();
			return new ClassEntry(getTarget().toRPC(), rpc.methods, rpc.getValues());
		}
	}
	
	public static MethodReport create(Method target) {
		MethodReport report = new MethodReport();
		report.setTarget(target);
		return report;
	}
	
	public static class MethodReport extends ClassesReport<Method, ClassesReport<?, ?>> {
		public InstanceEntry toEntry() {
			ClassesReport<Method, ClassesReport<?, ?>> rpc = toRPC();
			return new InstanceEntry(getTarget().toMethodString(), rpc.getValues());
		}
	}

}
