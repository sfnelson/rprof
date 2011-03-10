package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.request.ClassRequest;

public class ClassService implements ClassRequest {

	protected Context context(String handle) {
		return ContextManager.getInstance().getContext(handle);
	}

	@Override
	public List<? extends Class> findClasses(String dataset, String pkg) {
		return context(dataset).findClasses(pkg);
	}

}