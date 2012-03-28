package nz.ac.vuw.ecs.rprofs.server.domain.id;

import nz.ac.vuw.ecs.rprofs.server.domain.Result;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 27/03/12
 */
public class ResultId extends Id<ResultId, Result> {

	public ResultId(ClazzId clazzId) {
		super(clazzId != null ? clazzId.getValue() : 0l);
	}

	public ResultId(long value) {
		super(value);
	}

	public ClazzId getClazzId() {
		return new ClazzId(getValue());
	}

	@Override
	public Class<Result> getTargetClass() {
		return Result.class;
	}
}
