/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class Collections {

	public static <T> List<T> newList() {
		return new ArrayList<T>();
	}
	
	public static <S,T> Map<S,T> newMap() {
		return new HashMap<S,T>();
	}
	
	public static <T> Set<T> newSet() {
		return new HashSet<T>();
	}
	
	public static <T extends Comparable<T>> void sort(List<T> list) {
		java.util.Collections.sort(list);
	}
}
