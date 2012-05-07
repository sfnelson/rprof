package nz.ac.vuw.ecs.rprofs.server.db;

import nz.ac.vuw.ecs.rprofs.server.data.util.RequestCreator;
import nz.ac.vuw.ecs.rprofs.server.data.util.RequestQuery;
import nz.ac.vuw.ecs.rprofs.server.data.util.RequestUpdater;
import nz.ac.vuw.ecs.rprofs.server.domain.Request;
import nz.ac.vuw.ecs.rprofs.server.domain.id.RequestId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 6/05/12
 */
public abstract class MongoRequestBuilder extends MongoBuilder<MongoRequestBuilder, RequestId, Request>
		implements RequestCreator<MongoRequestBuilder>, RequestQuery<MongoRequestBuilder>, RequestUpdater<MongoRequestBuilder> {
	@Override
	public MongoRequestBuilder init(Request value) {
		b.append("_id", value.getId().getValue());
		setHasWriteLock(value.hasWriteLock());
		setHostname(value.getHostname());
		return this;
	}

	@Override
	public MongoRequestBuilder setHasWriteLock(boolean hasWriteLock) {
		b.append("lock", hasWriteLock);
		return this;
	}

	@Override
	public MongoRequestBuilder setHostname(String hostname) {
		b.append("hostname", hostname);
		return this;
	}

	@Override
	public Request get() {
		Request req = new Request(new RequestId(b.getLong("_id")));
		req.setHasWriteLock(b.getBoolean("lock", false));
		req.setHostname(b.getString("hostname"));
		return req;
	}
}
