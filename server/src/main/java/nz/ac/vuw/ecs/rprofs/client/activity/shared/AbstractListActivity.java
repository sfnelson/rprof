package nz.ac.vuw.ecs.rprofs.client.activity.shared;

import com.google.common.collect.Lists;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.Request;

import java.util.List;

public abstract class AbstractListActivity<E> extends AbstractActivity {

	private List<E> current;

	private boolean stopped = false;

	public AbstractListActivity() {
		current = Lists.newArrayList();
	}

	protected void refresh(Request<List<E>> request) {
		request.fire(new Receiver<List<E>>() {
			@Override
			public void onSuccess(List<E> result) {
				if (!stopped) {
					update(result);
				}
			}
		});
	}

	private void update(List<E> list) {
		List<E> toRemove = Lists.newArrayList();
		List<E> toAdd = Lists.newArrayList();

		toRemove.addAll(current);
		toRemove.removeAll(list);

		toAdd.addAll(list);
		toAdd.removeAll(current);

		for (E e : toRemove) {
			removeEntry(e);
		}

		current.removeAll(toRemove);

		for (E e : toAdd) {
			addEntry(e);
		}

		for (E e : list) {
			updateEntry(e);
		}

		current = list;
	}

	@Override
	public void onStop() {
		for (E e : current) {
			removeEntry(e);
		}

		stopped = true;
	}

	protected abstract void addEntry(E e);

	protected abstract void updateEntry(E e);

	protected abstract void removeEntry(E e);
}
