package nz.ac.vuw.ecs.rprofs.server.context;

import com.google.common.annotations.VisibleForTesting;
import com.mongodb.DB;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public class Context {

	@VisibleForTesting
	@Autowired
	Database db;

	private ThreadLocal<Dataset> dataset;
	private ThreadLocal<DB> database;

	public Context() {
		dataset = new ThreadLocal<Dataset>();
		database = new ThreadLocal<DB>();
	}

	public void setDataset(@NotNull Dataset dataset) {
		this.dataset.set(dataset);
		this.database.set(db.getDatabase(dataset));
	}

	public void clear() {
		this.dataset.remove();
		this.database.remove();
	}

	@Nullable
	public DB getDB() {
		return database.get();
	}

	@Nullable
	public Dataset getDataset() {
		return dataset.get();
	}
}
