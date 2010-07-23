package nz.ac.vuw.ecs.rprofs.client.data;

import java.io.Serializable;

public class LogRecord implements Serializable {
	
	private static final long serialVersionUID = -2196809197295190606L;
	
	public int index;
	public long threadId;
	public String event;
	public String className;
	public String methodName;
	public int classNumber;
	public int methodNumber;
	public long[] arguments;
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(threadId);
		s.append(" ");
		s.append(event);
		s.append(" ");
		s.append(className);
		s.append(".");
		s.append(methodName);
		s.append("(");
		if (arguments.length > 0) {
			s.append(arguments[0]);
			for (int i = 1; i < arguments.length; i++) {
				s.append(", ");
				s.append(arguments[i]);
			}
		}
		s.append(")");
		return s.toString();
	}
}
