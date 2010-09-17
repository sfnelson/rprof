/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.reports;

import java.io.Serializable;
import java.util.Set;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.data.Report.ClassEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.InstanceEntry;
import nz.ac.vuw.ecs.rprofs.client.data.Report.PackageEntry;


/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class InstanceReport<P, T, C> {

	public final InstanceReport<?, P, T> parent;
	public final Set<InstanceReport<T, C, ?>> children;
	public final T target;
	
	int classes;
	int instances;
	int reads;
	int writes;
	int ereads;
	int ewrites;
	
	public InstanceReport(InstanceReport<?, P, T> parent, T target) {
		this.parent = parent;
		this.children = Collections.newSet();
		this.target = target;
		if (parent != null) parent.children.add(this);
	}
	
	private InstanceReport(InstanceReport<P, T, C> o) {
		this.parent = null;
		this.children = null;
		this.target = o.target;
		this.classes = o.classes;
		this.instances = o.instances;
		this.reads = o.reads;
		this.writes = o.writes;
		this.ereads = o.ereads;
		this.ewrites = o.ewrites;
	}
	
	private InstanceReport<P, T, C> toRPC() {
		InstanceReport<P, T, C> result = new InstanceReport<P, T, C>(this);
		if (!children.isEmpty()) {
			for (InstanceReport<T, C, ?> c: children) {
				InstanceReport<T, C, ?> child = c.toRPC();
				result.classes += child.classes;
				result.instances += child.instances;
				result.reads += child.reads;
				result.writes += child.writes;
				result.ereads += child.ereads;
				result.ewrites += child.ewrites;
			}
		}
		return result;
	}
	
	public void toEntry(PackageEntry e) {
		InstanceReport<P, T, C> rpc = toRPC();
		Serializable[] result = new Serializable[] { rpc.reads, rpc.writes, rpc.ereads, rpc.ewrites };
		e.values = result;
		e.classes = rpc.classes;
		e.instances = rpc.instances;
	}
	
	public void toEntry(ClassEntry e) {
		InstanceReport<P, T, C> rpc = toRPC();
		Serializable[] result = new Serializable[] { rpc.reads, rpc.writes, rpc.ereads, rpc.ewrites };
		e.values = result;
		e.instances = rpc.instances;
	}
	
	public void toEntry(InstanceEntry e) {
		InstanceReport<P, T, C> rpc = toRPC();
		Serializable[] result = new Serializable[] { rpc.reads, rpc.writes, rpc.ereads, rpc.ewrites };
		e.values = result;
	}
	
	public static <P, T, C> InstanceReport<P, T, C> create(InstanceReport<?, P, T> parent, T target) {
		return new InstanceReport<P, T, C>(parent, target);
	}
}
