package nz.ac.vuw.ecs.rprofs.server.reports;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 9/11/11
 */
public interface MapReduceTask<T> extends Runnable {
	public void map();

	public void mapVolatile(T input);

	public void flush();

	public void reduce();
}
