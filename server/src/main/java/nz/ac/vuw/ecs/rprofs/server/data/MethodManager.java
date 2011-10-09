package nz.ac.vuw.ecs.rprofs.server.data;

import com.google.web.bindery.requestfactory.shared.Locator;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 10/10/11
 */
@Configurable
public class MethodManager extends Locator<Method, MethodId> {

	@Autowired
	Database database;

	@Override
	public Method create(Class<? extends Method> aClass) {
		return new Method();
	}

	@Override
	public Method find(Class<? extends Method> aClass, MethodId methodId) {
		return database.findEntity(methodId);
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
