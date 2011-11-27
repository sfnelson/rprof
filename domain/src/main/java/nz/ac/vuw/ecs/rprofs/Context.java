package nz.ac.vuw.ecs.rprofs;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;

public class Context {

	private static ThreadLocal<Dataset> DATASET = new ThreadLocal<Dataset>();

	public static void setDataset(@NotNull Dataset dataset) {
		Context.DATASET.set(dataset);
	}

	public static void clear() {
		Context.DATASET.remove();
	}

	@Nullable
	public static Dataset getDataset() {
		return Context.DATASET.get();
	}
}
