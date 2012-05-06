package nz.ac.vuw.ecs.rprofs.server;

import java.net.UnknownHostException;
import java.util.Set;

import com.google.web.bindery.requestfactory.server.ExceptionHandler;
import com.google.web.bindery.requestfactory.server.ServiceLayerDecorator;
import com.google.web.bindery.requestfactory.shared.*;
import com.google.web.bindery.requestfactory.shared.ServiceLocator;

import com.google.inject.*;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.mongodb.Mongo;
import javax.validation.*;
import nz.ac.vuw.ecs.rprofs.server.data.ClassManager;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.data.EventManager;
import nz.ac.vuw.ecs.rprofs.server.data.RequestManager;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.id.RequestId;
import nz.ac.vuw.ecs.rprofs.server.request.*;
import org.eclipse.jetty.continuation.ContinuationFilter;
import org.slf4j.LoggerFactory;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 9/11/11
 */
public class ServletConfig extends GuiceServletContextListener {
	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new ServletModule() {
			@Override
			protected void configureServlets() {
				bind(ContinuationFilter.class).in(Singleton.class);

				filter("/gwtRequest*").through(GwtRequest.class);

				serve("/gwtRequest*").with(RequestFactoryServlet.class);
				serve("/logger").with(Logger.class);
				serve("/process").with(Process.class);
				serve("/start").with(Start.class);
				serve("/stop").with(Stop.class);
				serve("/weaver").with(Weave.class);
				filter("/worker").through(ContinuationFilter.class);
				serve("/worker").with(Workers.class);

				bind(ServiceLayerDecorator.class).to(InjectingServiceLayerDecorator.class);

				bind(Database.class).asEagerSingleton();

				bind(ClazzService.class).to(ClassManager.class);
				bind(DatasetService.class).to(DatasetManager.class);
				bind(EventService.class).to(EventManager.class);
				bind(FieldService.class).to(ClassManager.class);
				bind(MethodService.class).to(ClassManager.class);
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

			@Provides
			RequestId getRequest(RequestManager manager) {
				return manager.createRequest();
			}

			@Provides
			@Singleton
			public ValidatorFactory getValidatorFactory(ConstraintValidatorFactory cf) {
				return Validation.byDefaultProvider().configure()
						.constraintValidatorFactory(cf)
						.buildValidatorFactory();
			}

			@Provides
			@Singleton
			public Validator getValidator(ValidatorFactory factory) {
				return factory.getValidator();
			}

			@Provides
			@Singleton
			public ConstraintValidatorFactory getConstraintValidatorFactory(final Injector injector) {
				return new ConstraintValidatorFactory() {
					@Override
					public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
						return injector.getInstance(key);
					}
				};
			}

			@Provides
			@Singleton
			public ExceptionHandler getExceptionHandler() {
				return new ExceptionHandler() {
					org.slf4j.Logger logger = LoggerFactory.getLogger(RequestFactory.class);

					@Override
					public ServerFailure createServerFailure(Throwable throwable) {
						logger.error(throwable.getMessage(), throwable);
						return new ServerFailure(throwable.getMessage(), throwable.getClass().getName(), null, true);
					}
				};
			}
		});
	}

	static class InjectingServiceLayerDecorator extends ServiceLayerDecorator {
		private final Injector injector;
		private final Validator validator;

		@Inject
		InjectingServiceLayerDecorator(Injector injector, Validator validator) {
			this.injector = injector;
			this.validator = validator;
		}

		@Override
		public <T extends Locator<?, ?>> T createLocator(Class<T> clazz) {
			return injector.getInstance(clazz);
		}

		@Override
		public Object createServiceInstance(Class<? extends RequestContext> requestContext) {
			Class<? extends ServiceLocator> serviceLocator = getTop().resolveServiceLocator(requestContext);

			if (serviceLocator == null) return null;

			return injector.getInstance(serviceLocator)
					.getInstance(requestContext.getAnnotation(Service.class).value());
		}

		@Override
		public <T> Set<ConstraintViolation<T>> validate(T domainObject) {
			return validator.validate(domainObject);
		}
	}
}
