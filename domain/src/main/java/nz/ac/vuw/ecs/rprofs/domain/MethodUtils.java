package nz.ac.vuw.ecs.rprofs.domain;

import nz.ac.vuw.ecs.rprofs.server.domain.Method;

import static org.objectweb.asm.Opcodes.*;

public class MethodUtils {

	public static boolean isNative(Method method) {
		return (ACC_NATIVE & method.getAccess()) != 0;
	}

	public static boolean isMain(Method method) {
		return "main".equals(method.getName())
				&& "([Ljava/lang/String;)V".equals(method.getDescription())
				&& (ACC_PUBLIC | ACC_STATIC) == method.getAccess(); // public, static
	}

	public static boolean isInit(Method method) {
		return "<init>".equals(method.getName());
	}

	public static boolean isCLInit(Method method) {
		return "<clinit>".equals(method.getName());
	}

	public static boolean isAgentInit(Method method) {
		return "_rprof_agent_init".equals(method.getName());
	}

	public static boolean isEquals(Method method) {
		return "equals".equals(method.getName())
				&& "(Ljava/lang/Object;)Z".equals(method.getDescription())
				&& ACC_PUBLIC == method.getAccess(); // public
	}

	public static boolean isHashCode(Method method) {
		return "hashCode".equals(method.getName())
				&& "()I".equals(method.getDescription())
				&& ACC_PUBLIC == method.getAccess(); // public
	}

	public static boolean isStatic(Method method) {
		return (ACC_STATIC & method.getAccess()) != 0; // static
	}

	public static boolean isPublic(Method method) {
		return (ACC_PUBLIC & method.getAccess()) != 0; // public
	}

	public static boolean hasArgs(Method method) {
		return !method.getDescription().contains("()");
	}
}
