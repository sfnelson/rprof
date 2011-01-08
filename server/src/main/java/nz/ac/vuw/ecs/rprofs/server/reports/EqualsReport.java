/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.reports;

import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.data.Report.ClassEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.InstanceEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.PackageEntry;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class EqualsReport<Target, Child extends EqualsReport<?, ?>> extends AbstractReport<Target, Child> {

	int classes;
	int fields;
	int equals;
	int hash;

	protected EqualsReport() {}

	private EqualsReport(EqualsReport<Target, Child> o) {
		setTarget(o.getTarget());
		this.classes = o.classes;
		this.fields = o.fields;
		this.equals = o.equals;
		this.hash = o.hash;
	}

	protected EqualsReport<Target, Child> toRPC() {
		EqualsReport<Target, Child> rpc = new EqualsReport<Target, Child>(this);
		for (Child child: getChildren()) {
			EqualsReport<?, ?> c = child.toRPC();
			rpc.classes += c.classes;
			rpc.fields += c.fields;
			rpc.equals += c.equals;
			rpc.hash += c.hash;
		}
		return rpc;
	}

	protected int[] getValues() {
		return new int[] { equals, hash };
	}

	public static PackageReport create(String target) {
		PackageReport report = new PackageReport();
		report.setTarget(target);
		return report;
	}

	public static class PackageReport extends EqualsReport<String, ClassReport> {
		public PackageEntry toEntry() {
			EqualsReport<String, ClassReport> rpc = toRPC();
			return new PackageEntry(getTarget(), rpc.classes, rpc.fields, rpc.getValues());
		}
	}

	public static ClassReport create(Class target) {
		ClassReport report = new ClassReport();
		report.setTarget(target);
		return report;
	}

	public static class ClassReport extends EqualsReport<Class, FieldReport> {
		Map<Field, FieldReport> fields = Collections.newMap();
		
		public ClassEntry toEntry() {
			EqualsReport<Class, FieldReport> rpc = toRPC();
			return new ClassEntry(getTarget().toRPC(), rpc.fields, rpc.getValues());
		}
	}

	public static FieldReport create(Field target) {
		FieldReport report = new FieldReport();
		report.setTarget(target);
		return report;
	}

	public static class FieldReport extends EqualsReport<Field, EqualsReport<?, ?>> {
		public InstanceEntry toEntry() {
			EqualsReport<Field, EqualsReport<?, ?>> rpc = toRPC();
			return new InstanceEntry(getTarget().toString(), rpc.getValues());
		}
	}

}
