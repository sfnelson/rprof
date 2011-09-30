package nz.ac.vuw.ecs.rprofs.server.data;

import com.google.common.annotations.VisibleForTesting;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import org.springframework.beans.factory.annotation.Configurable;

import javax.validation.constraints.NotNull;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 29/09/11
 */
@Configurable
public class InstanceManager {

	@VisibleForTesting
	@NotNull
	Database database;


}
