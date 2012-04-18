import java.util.HashMap;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 18/04/12
 */
public class HashMapError {
	public static void main(String args[]) {
		HashMap<Object, Object> map = new HashMap<Object, Object>();
		for (int i = 0; i < 64; i++) {
			map.put(new Object(), new Object());
		}
	}
}
