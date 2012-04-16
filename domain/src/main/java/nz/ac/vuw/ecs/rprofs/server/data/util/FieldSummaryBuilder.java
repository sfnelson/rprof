package nz.ac.vuw.ecs.rprofs.server.data.util;

import nz.ac.vuw.ecs.rprofs.server.domain.FieldSummary;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldSummaryId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 10/04/12
 */
public interface FieldSummaryBuilder<B extends FieldSummaryBuilder<B>> extends Builder<B, FieldSummaryId, FieldSummary> {
	B setId(FieldSummaryId id);

	B setName(String name);

	B setDescription(String description);

	B setPackageName(String name);

	B setDeclaredFinal(boolean isDeclaredFinal);

	B setStationary(boolean isStationary);

	B setConstructed(boolean isConstructed);

	B setFinal(boolean isFinal);

	B setInstances(int instances);

	B setReads(long reads);

	B setWrites(long writes);
}
