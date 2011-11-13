package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import com.google.web.bindery.requestfactory.shared.Locator;

import com.google.inject.Inject;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;
import nz.ac.vuw.ecs.rprofs.server.request.MethodService;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 10/10/11
 */
@Configurable
public class MethodManager extends Locator<Method, MethodId> implements MethodService {

	private final Database database;

	@Inject
	MethodManager(Database database) {
		this.database = database;
	}

	@Override
	public Method create(Class<? extends Method> aClass) {
		return new Method();
	}

	@Override
	public Method find(Class<? extends Method> aClass, MethodId methodId) {
		return database.findEntity(methodId);
	}

	@Override
	public Method getMethod(MethodId methodId) {
		return database.findEntity(methodId);
	}

	@Override
	public List<? extends Method> findMethods(ClazzId clazzId) {
		return null;
	}

	@Override
	public Class<Method> getDomainType() {
		return Method.class;
	}

	@Override
	public MethodId getId(Method method) {
		return method.getId();
	}

	@Override
	public Class<MethodId> getIdType() {
		return MethodId.class;
	}

	@Override
	public Integer getVersion(Method method) {
		return method.getVersion();
	}
}
