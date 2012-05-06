package nz.ac.vuw.ecs.rprofs.server.domain.id;

import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Request;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 6/05/12
 */
public class RequestId extends Id<RequestId, Request> {

	private static int lastId = 0;

	public static RequestId create(Dataset dataset) {
		return new RequestId(dataset.getId().getDatasetIndex(), ++lastId);
	}

	public RequestId() {
	}

	public Class<Request> getTargetClass() {
		return Request.class;
	}

	public RequestId(short dataset, int index) {
		super((((long) dataset) << 48) | index);
	}

	public RequestId(long id) {
		super(id);
	}

	public short getDatasetIndex() {
		return (short) ((getValue() >>> 48) & 0xFFFF);
	}

	public void setDatasetIndex(short datasetIndex) {
		// noop provided for gwt
	}

	public int getRequestIndex() {
		return (int) getValue();
	}

	public void setRequestIndex(int classIndex) {
		// noop provided for gwt
	}

	@Override
	public String toString() {
		return String.format("%d.%d", getDatasetIndex(), getRequestIndex());
	}
}
