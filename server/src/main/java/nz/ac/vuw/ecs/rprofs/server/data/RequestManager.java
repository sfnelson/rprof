package nz.ac.vuw.ecs.rprofs.server.data;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Request;
import nz.ac.vuw.ecs.rprofs.server.domain.id.RequestId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 6/05/12
 */
@Singleton
public class RequestManager {

	private final Logger log = LoggerFactory.getLogger(RequestManager.class);
	private final Database database;

	@Inject
	RequestManager(Database database) {
		this.database = database;
	}

	public synchronized RequestId createRequest() {
		return database.getRequestCreator().store();
	}

	public synchronized void releaseRequest(RequestId request) {
		Request rq = database.findEntity(request);
		//if (rq != null) {
		database.deleteEntity(rq);
		//}
		notifyAll();
	}

	public synchronized void waitUntilDone() throws InterruptedException {
		while (database.getRequestQuery().count() > 0) {
			log.debug("waiting for requests to finish");
			wait(10000);
		}
	}
}
