package nz.ac.vuw.ecs.rprofs.server;

import com.google.web.bindery.requestfactory.server.ExceptionHandler;
import com.google.web.bindery.requestfactory.server.ServiceLayerDecorator;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 9/11/11
 */
@Singleton
public class RequestFactoryServlet extends com.google.web.bindery.requestfactory.server.RequestFactoryServlet {

	@Inject
	public RequestFactoryServlet(ExceptionHandler exceptionHandler, ServiceLayerDecorator serviceDecorators) {
		super(exceptionHandler, serviceDecorators);
	}
}
