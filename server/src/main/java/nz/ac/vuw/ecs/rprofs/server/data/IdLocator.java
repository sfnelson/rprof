package nz.ac.vuw.ecs.rprofs.server.data;

import com.google.web.bindery.requestfactory.shared.Locator;
import nz.ac.vuw.ecs.rprofs.server.model.Id;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 13/09/11
 */
public class IdLocator extends Locator<Id, Long> {

	@Override
	public Id create(Class<? extends Id> type) {
		try {
			return type.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Id find(Class<? extends Id> type, Long id) {
		Id realId = create(type);
		realId.setValue(id);
		return realId;
	}

	@Override
	public Class<Id> getDomainType() {
		return Id.class;
	}

	@Override
	public Long getId(Id id) {
		return id.longValue();
	}

	@Override
	public Class<Long> getIdType() {
		return Long.class;
	}

	@Override
	public Object getVersion(Id id) {
		return 1;
	}
}
