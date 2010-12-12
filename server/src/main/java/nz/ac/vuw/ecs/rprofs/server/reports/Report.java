/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.reports;

import java.util.Set;

import nz.ac.vuw.ecs.rprofs.client.Collections;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public interface Report<Target, Children> {
	public Target getTarget();
	public Iterable<Children> getChildren();
}

abstract class AbstractReport<T, C extends AbstractReport<?, ?>> implements Report<T, C> {
	
	private final Set<C> children = Collections.newSet();
	private T target;
	
	public void setTarget(T target) {
		this.target = target;
	}
	
	public T getTarget() {
		return target;
	}
	
	public void addChild(C child) {
		children.add(child);
	}
	
	public Iterable<C> getChildren() {
		return children;
	}
}