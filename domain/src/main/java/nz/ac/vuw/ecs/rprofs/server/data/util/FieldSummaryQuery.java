package nz.ac.vuw.ecs.rprofs.server.data.util;

import nz.ac.vuw.ecs.rprofs.server.domain.FieldSummary;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldSummaryId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 10/04/12
 */
public interface FieldSummaryQuery<Q extends FieldSummaryQuery<Q>> extends FieldSummaryBuilder<Q>, Query<FieldSummaryId, FieldSummary> {

}
