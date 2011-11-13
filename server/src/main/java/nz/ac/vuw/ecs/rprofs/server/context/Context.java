package nz.ac.vuw.ecs.rprofs.server.context;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;

@Singleton
public class Context {

	private ThreadLocal<Dataset> dataset;

	@Inject
	public Context() {
		dataset = new ThreadLocal<Dataset>();
	}

	public void setDataset(@NotNull Dataset dataset) {
		this.dataset.set(dataset);
	}

	public void clear() {
		this.dataset.remove();
	}

	@Nullable
	public Dataset getDataset() {
		return dataset.get();
	}
}
