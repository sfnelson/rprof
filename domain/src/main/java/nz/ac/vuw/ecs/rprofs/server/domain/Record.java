package nz.ac.vuw.ecs.rprofs.server.domain;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 22/09/12
 */
public class Record {

	private boolean isReadSettled;
	private boolean isObjectReadSettled;
	private boolean isWriteSettled;
	private boolean isObjectWriteSettled;
	private boolean isConstructorSettled;
	private boolean isEqualsSettled;
	private boolean isHashSettled;

	private int numClasses;
	private int numObjects;
	private int numFields;
}
