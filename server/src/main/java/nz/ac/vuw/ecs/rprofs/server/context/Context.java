package nz.ac.vuw.ecs.rprofs.server.context;

import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public class Context {

	@Autowired
	private Database db;

	private ThreadLocal<Dataset> dataset;

	public Context() {
		dataset = new ThreadLocal<Dataset>();
	}

	public Context(Database db) {
		this();
		this.db = db;
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
