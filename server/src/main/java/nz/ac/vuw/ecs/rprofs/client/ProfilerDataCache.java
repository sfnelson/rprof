package nz.ac.vuw.ecs.rprofs.client;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.Receiver;
import nz.ac.vuw.ecs.rprofs.client.request.*;
import nz.ac.vuw.ecs.rprofs.client.request.id.*;

import java.util.List;
import java.util.Map;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 17/09/11
 */
public class ProfilerDataCache implements DataProvider {

	Provider<ClazzRequest> classes;
	Provider<EventRequest> events;
	Provider<MethodRequest> methods;
	Provider<FieldRequest> fields;

	Map<Long, Integer> threadCache = Maps.newHashMap();
	Map<Long, ClazzProxy> classCache = Maps.newHashMap();
	Map<Long, MethodProxy> methodCache = Maps.newHashMap();
	Map<Long, FieldProxy> fieldCache = Maps.newHashMap();

	@Inject
	public ProfilerDataCache(Provider<ClazzRequest> classes, Provider<EventRequest> events,
							 Provider<MethodRequest> methods, Provider<FieldRequest> fields) {
		this.classes = classes;
		this.methods = methods;
		this.fields = fields;
		this.events = events;
	}

	@Override
	public boolean hasThread(InstanceIdProxy thread) {
		return getThreadIndex(thread) > 0;
	}

	@Override
	public int getThreadIndex(InstanceIdProxy thread) {
		if (thread == null) return 0;
		if (threadCache.containsKey(thread.getValue())) {
			return threadCache.get(thread.getValue());
		} else {
			events.get().findThreads().fire(new ThreadReceiver());
			return -1;
		}
	}

	@Override
	public int getNumThreads() {
		return threadCache.size();
	}

	@Override
	public <I extends HasId<T>, T extends EntityProxy> boolean hasEntity(I id) {
		return getEntity(id) != null;
	}

	@Override
	public <I extends HasId<T>, T extends EntityProxy> T getEntity(I id) {
		if (id == null || id.getValue() == 0) return null;
		if (id instanceof ClazzIdProxy) {
			if (classCache.containsKey(id.getValue())) {
				return (T) classCache.get(id.getValue());
			} else {
				classes.get().getClazz((ClazzIdProxy) id)
						.with("id", "parent")
						.to(new ClazzReceiver());
				return null;
			}
		}
		if (id instanceof MethodIdProxy) {
			if (methodCache.containsKey(id.getValue())) {
				return (T) methodCache.get(id.getValue());
			} else {
				methods.get().getMethod((MethodIdProxy) id)
						.with("id", "owner")
						.to(new MethodReceiver());
				return null;
			}
		}
		if (id instanceof FieldIdProxy) {
			if (fieldCache.containsKey(id.getValue())) {
				return (T) fieldCache.get(id.getValue());
			} else {
				fields.get().getField((FieldIdProxy) id)
						.with("id", "owner")
						.to(new FieldReceiver());
				return null;
			}
		}
		GWT.log(id.getClass().getName() + " not available");
		return null;
	}

	private class ThreadReceiver extends Receiver<List<? extends InstanceIdProxy>> {
		@Override
		public void onSuccess(List<? extends InstanceIdProxy> response) {
			threadCache.clear();
			for (int i = 1; i <= response.size(); i++) {
				threadCache.put(response.get(i - 1).getValue(), i);
			}
			GWT.log(response.size() + " threads available");
		}
	}

	private class ClazzReceiver extends Receiver<ClazzProxy> {
		@Override
		public void onSuccess(ClazzProxy response) {
			GWT.log(response.getName() + " available");
			classCache.put(response.getId().getValue(), response);
		}
	}

	private class MethodReceiver extends Receiver<MethodProxy> {
		@Override
		public void onSuccess(MethodProxy response) {
			GWT.log(response.getName() + " available");
			methodCache.put(response.getId().getValue(), response);
		}
	}

	private class FieldReceiver extends Receiver<FieldProxy> {
		@Override
		public void onSuccess(FieldProxy response) {
			fieldCache.put(response.getId().getValue(), response);
		}
	}
}
