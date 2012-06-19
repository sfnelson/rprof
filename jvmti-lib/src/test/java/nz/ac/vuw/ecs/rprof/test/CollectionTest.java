package nz.ac.vuw.ecs.rprof.test;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 30/05/12
 */
public class CollectionTest {

	int f;

	public CollectionTest(int val) {
		f = val;
	}

	public boolean equals(Object o) {
		return this == o;
	}

	public static void main(String args[]) {
		List<CollectionTest> list = new ArrayList<CollectionTest>();

		list.add(new CollEq());
		System.out.println(list.get(0).f + " " + list.get(0).equals(null));

		CollectionTest c = new Eq();
		System.out.println(c.f + " " + c.equals(null));

		list.add(new Coll());
		System.out.println(list.get(1).f);

		System.out.println(new None().f);
	}

	static class CollEq extends CollectionTest {
		public CollEq() {
			super(1);
		}
	}

	static class Eq extends CollectionTest {
		public Eq() {
			super(2);
		}
	}

	static class Coll extends CollectionTest {
		public Coll() {
			super(3);
		}
	}

	static class None extends CollectionTest {
		public None() {
			super(4);
		}
	}
}
