package nz.ac.vuw.ecs.rprofs.client.data;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LogData extends LogInfo implements IsSerializable {

	public long index;
	public long thread;
	public int event;
	public int cnum;
	public int mnum;
	public long[] args;
	
	public LogData() {}
	public LogData(long index, long thread, int event, int cnum, int mnum, long[] args) {
		this.index = index;
		this.thread = thread;
		this.event = event;
		this.cnum = cnum;
		this.mnum = mnum;
		this.args = args;
	}

	@Override
	public long[] getArguments() {
		// TODO Auto-generated method stub
		return null;
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
