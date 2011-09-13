package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.domain.Method;

public class MethodUtils {

	public static boolean isNative(Method method) {
		return (0x800 & method.getAccess()) != 0;
	}

	public static boolean isMain(Method method) {
		return "main".equals(method.getName())
				&& "([Ljava/lang/String;)V".equals(method.getDescription())
				&& (0x1 | 0x8) == method.getAccess(); // public, static
	}

	public static boolean isInit(Method method) {
		return "<init>".equals(method.getName());
	}

	public static boolean isCLInit(Method method) {
		return "<clinit>".equals(method.getName());
	}

	public static boolean isEquals(Method method) {
		return "equals".equals(method.getName())
				&& "(Ljava/lang/Object;)Z".equals(method.getDescription())
				&& 0x1 == method.getAccess(); // public
	}

	public static boolean isHashCode(Method method) {
		return "hashCode".equals(method.getName())
				&& "()I".equals(method.getDescription())
				&& 0x1 == method.getAccess(); // public
	}

	public static boolean isStatic(Method method) {
		return (0x8 & method.getAccess()) != 0; // static
	}
}
