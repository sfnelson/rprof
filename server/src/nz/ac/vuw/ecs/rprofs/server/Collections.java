/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class Collections {

	public static <T> List<T> newList() {
		return new ArrayList<T>();
	}
}
