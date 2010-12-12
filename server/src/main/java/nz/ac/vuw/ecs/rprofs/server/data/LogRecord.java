/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import nz.ac.vuw.ecs.rprofs.client.data.LogInfo;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
@Entity
@Table( name = "events" )
public class LogRecord extends LogInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	public long index;

	public long thread;
	public int event;
	public int cnum;
	public int mnum;
	public long[] args;

	public LogRecord() {}

	@Override
	public long[] getArguments() {
		return args;
	}

	@Override
	public int getClassNumber() {
		return cnum;
	}

	@Override
	public int getEvent() {
		return event;
	}

	@Override
	public long getIndex() {
		return index;
	}

	@Override
	public int getMethodNumber() {
		return mnum;
	}

	@Override
	public long getThread() {
		return thread;
	}
}
