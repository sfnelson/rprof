package nz.ac.vuw.ecs.rprofs.server.domain;

import javax.validation.constraints.NotNull;
import nz.ac.vuw.ecs.rprofs.server.domain.id.RequestId;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 6/05/12
 */
public class Request implements DataObject<RequestId, Request> {

	@NotNull
	private RequestId id;

	private boolean hasWriteLock;

	private String hostname;

	public Request(RequestId id) {
		this.id = id;
	}

	@Override
	public RequestId getId() {
		return id;
	}

	public void setHasWriteLock(boolean hasWriteLock) {
		this.hasWriteLock = hasWriteLock;
	}

	public boolean hasWriteLock() {
		return hasWriteLock;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getHostname() {
		return hostname;
	}
}
