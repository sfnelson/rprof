/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class Collections {

	public static <T> ArrayList<T> newList() {
		return new ArrayList<T>();
	}

	public static <S,T> HashMap<S,T> newMap() {
		return new HashMap<S,T>();
	}

	public static <T> HashSet<T> newSet() {
		return new HashSet<T>();
	}

	public static <T> Stack<T> newStack() {
		return new Stack<T>();
	}

	public static <T extends Comparable<? super T>> void sort(List<T> list) {
		java.util.Collections.sort(list);
	}

	public static <T> void sort(List<T> list, Comparator<? super T> comparator) {
		java.util.Collections.sort(list, comparator);
	}

	public static <S, T> Map<S, T> immutable(Map<S, T> map) {
		return java.util.Collections.unmodifiableMap(map);
	}

	public static <T> Set<T> immutable(Set<T> set) {
		return java.util.Collections.unmodifiableSet(set);
	}

	public static <T> List<T> immutable(List<T> list) {
		return java.util.Collections.unmodifiableList(list);
	}

	public static <T> Collection<T> immutable(Collection<T> collection) {
		return java.util.Collections.unmodifiableCollection(collection);
	}

	public static final Comparator<Object> HASH_COMPARATOR = new Comparator<Object>() {
		@Override
		public int compare(Object o1, Object o2) {
			return o2.hashCode() - o1.hashCode();
		}
	};

	public static final Comparator<Object> TO_STRING_COMPARATOR = new Comparator<Object>() {
		@Override
		public int compare(Object o1, Object o2) {
			return o2.toString().compareTo(o1.toString());
		}
	};

	public static <T> List<T> emptyList() {
		return java.util.Collections.emptyList();
	}
}
