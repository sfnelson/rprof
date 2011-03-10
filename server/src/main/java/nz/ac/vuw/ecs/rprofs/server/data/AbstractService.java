package nz.ac.vuw.ecs.rprofs.server.data;

public abstract class AbstractService {

	protected Context context(String handle) {
		return ContextManager.getInstance().getContext(handle);
	}

	protected Context context(short id) {
		return ContextManager.getInstance().getContext(id);
	}

}
