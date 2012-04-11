package nz.ac.vuw.ecs.rprofs.server.data.util;

import nz.ac.vuw.ecs.rprofs.server.domain.ClassSummary;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassSummaryId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 27/03/12
 */
public interface ClassSummaryUpdater<U extends ClassSummaryUpdater<U>> extends ClassSummaryBuilder<U>, Updater<ClassSummaryId, ClassSummary> {

}
