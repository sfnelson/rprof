package nz.ac.vuw.ecs.rprofs.worker;

import java.net.UnknownHostException;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.mongodb.Mongo;
import nz.ac.vuw.ecs.rprofs.server.db.Database;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 28/11/11
 */
public class WorkerModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(Database.class).asEagerSingleton();
	}

	@Provides
	@Singleton
	Mongo getMongo() throws UnknownHostException {
		String params = System.getProperty("mongo");
		if (params != null) {
			return new Mongo(params);
		} else {
			return new Mongo();
		}
	}
}
