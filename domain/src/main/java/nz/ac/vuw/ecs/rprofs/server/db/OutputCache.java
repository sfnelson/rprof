package nz.ac.vuw.ecs.rprofs.server.db;

import java.lang.management.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import javax.management.*;
import nz.ac.vuw.ecs.rprofs.server.data.util.Creator;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 6/12/11
 */
class OutputCache<I extends Id<I, T>, T extends DataObject<I, T>> implements NotificationListener {

	private static class MemoryMonitor {
		private static final float THRESHOLD = 0.5f;
		private static final NotificationFilter FILTER = new NotificationFilter() {
			@Override
			public boolean isNotificationEnabled(Notification notification) {
				return notification.getType().equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED);
			}
		};
		private final NotificationEmitter emitter;

		public MemoryMonitor() {
			MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
			emitter = (NotificationEmitter) mem;

			MemoryPoolMXBean pool = null;
			for (MemoryPoolMXBean p : ManagementFactory.getMemoryPoolMXBeans()) {
				if (p.getType() == MemoryType.HEAP && p.isCollectionUsageThresholdSupported()) {
					pool = p;
				}
			}

			if (pool != null) {
				pool.setCollectionUsageThreshold((long) (pool.getUsage().getMax() * THRESHOLD));
			}
		}

		public void register(NotificationListener listener) {
			emitter.addNotificationListener(listener, FILTER, null);
		}

		public void deregister(NotificationListener listener) throws ListenerNotFoundException {
			emitter.removeNotificationListener(listener);
		}
	}


	private static final Logger log = LoggerFactory.getLogger(OutputCache.class);
	private static final MemoryMonitor MONITOR = new MemoryMonitor();

	private static final int NUM_MAPS = 16;
	private static final int MAP_SIZE = 65536;

	private final Deque<Map<I, T>> maps = new ArrayDeque<Map<I, T>>();

	private final Creator<?, I, T> output;

	public OutputCache(Creator<?, I, T> output) {
		this.output = output;

		MONITOR.register(this);
	}

	@Override
	protected void finalize() throws Throwable {
		MONITOR.deregister(this);
	}

	public synchronized int size() {
		int size = 0;
		for (Map<?, ?> m : maps) {
			size += m.size();
		}
		return size;
	}

	public synchronized boolean isEmpty() {
		for (Map<?, ?> m : maps) {
			if (!m.isEmpty()) return false;
		}
		return true;
	}

	public synchronized boolean containsKey(Object key) {
		for (Map<?, ?> m : maps) {
			if (m.containsKey(key)) return true;
		}
		return false;
	}

	public synchronized T get(I key) {
		for (Map<?, T> m : maps) {
			if (m.containsKey(key)) return m.get(key);
		}
		return null;
	}

	public synchronized T put(I key, T value) {
		T toReturn = null;
		for (Map<?, T> m : maps) {
			if (m.containsKey(key)) {
				toReturn = m.remove(key);
				if (m.isEmpty()) {
					maps.remove(m);
				}
				break;
			}
		}
		Map<I, T> map = maps.isEmpty() ? null : maps.getLast();
		if (map == null || map.size() >= MAP_SIZE) {
			map = new HashMap<I, T>(MAP_SIZE * 2);
			maps.addLast(map);
		}

		map.put(key, value);

		while (maps.size() > NUM_MAPS) {
			Map<?, T> m = maps.getFirst();
			for (T v : m.values()) {
				output.init(v).store();
			}
		}

		return toReturn;
	}

	public synchronized void flush() {
		for (Map<?, T> m : maps) {
			for (T value : m.values()) {
				output.init(value).store();
			}
		}
		maps.clear();
	}

	@Override
	public synchronized void handleNotification(Notification notification, Object handback) {
		int num = maps.size() / 2;
		log.debug("flushing {} to reclaim memory ({} cached)", num, size());
		for (int i = 0; i < num; i++) {
			Map<?, T> m = maps.removeFirst();
			if (m != null) {
				for (T value : m.values()) {
					output.init(value).store();
				}
			}
		}
	}
}
