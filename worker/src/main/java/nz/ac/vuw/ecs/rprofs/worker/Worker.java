package nz.ac.vuw.ecs.rprofs.worker;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Calendar;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import javax.annotation.Nullable;
import nz.ac.vuw.ecs.rprofs.Context;
import nz.ac.vuw.ecs.rprofs.server.data.util.EventCreator;
import nz.ac.vuw.ecs.rprofs.server.data.util.Query;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.*;
import nz.ac.vuw.ecs.rprofs.server.reports.InstanceMapReduce;
import nz.ac.vuw.ecs.rprofs.server.reports.ReduceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 28/11/11
 */
public class Worker {

	//		#define MAX_PARAMETERS 16
	//		struct EventRecord {
	//			long int id;
	//			long int thread;
	//			char message[255];
	//			int cnum;
	//			int mnum;
	//			int len;
	//			long int params[MAX_PARAMETERS];
	//		}
	public static final int MAX_PARAMETERS = 20;
	public static final int RECORD_LENGTH = 8 + 8 + 4 + 4 + 4 + 4 + MAX_PARAMETERS * 8;

	public static final void main(String[] args) throws Exception {
		Injector injector = Guice.createInjector(new WorkerModule());

		String hostname = InetAddress.getLocalHost().getHostName();

		Worker worker = injector.getInstance(Worker.class);
		worker.run(hostname);
	}

	@Nullable
	private static InstanceId parseObjectId(Dataset ds, long id) {
		if (id == 0) return InstanceId.NULL;
		return InstanceId.create(ds, id);
	}

	private final Logger log = LoggerFactory.getLogger(Worker.class);

	private final Database database;

	@Inject
	Worker(Database database) {
		this.database = database;
	}

	public void run(String hostname) throws Exception {
		String server = System.getProperty("rprofs");
		URL url = new URL("http://" + server + "/worker");
		RequestId requestId = null;
		String request = null;
		String handle = null;

		Handler current = null;

		while (true) {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			if (current != null) {
				connection.setRequestProperty("HasCache", "true");
			}
			if (request != null) {
				connection.setRequestProperty("RequestId", request);
			}
			if (handle != null) {
				connection.setRequestProperty("Dataset", handle);
			}
			if (hostname != null) {
				connection.setRequestProperty("Hostname", hostname);
			}

			connection.connect();

			if (connection.getResponseCode() / 100 != 2) {
				log.error("Bad response! {}", connection.getResponseCode());
				if (current != null) {
					current.flush();
				}
				break;
			}

			handle = connection.getHeaderField("Dataset");
			request = connection.getHeaderField("RequestId");
			requestId = RequestId.create(request);

			Dataset dataset = getDataset(handle);
			boolean flush = connection.getHeaderField("Flush") != null;
			int length = connection.getContentLength();

			if (current != null && (flush || !current.ds.equals(dataset))) {
				current.flush();
				current = null;
				continue;
			}

			if (dataset == null) {
				if (connection.getResponseCode() == 201 || length == 0)
					log.trace("no data returned");
				else
					log.error("couldn't find dataset! throwing away response ({} bytes)", length);
				try {
					Thread.sleep(30000);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
					System.exit(1);
				}
				continue;
			}

			if (current == null) {
				current = new Handler(hostname, dataset, requestId);
			}

			int records = Integer.valueOf(connection.getHeaderField("Records"));

			DataInputStream dis = new DataInputStream(connection.getInputStream());

			current.process(length, records, dis);
		}
	}

	private Dataset getDataset(String name) {
		Query.Cursor<? extends Dataset> cursor = database.getDatasetQuery().find();
		while (cursor.hasNext()) {
			Dataset ds = cursor.next();
			if (ds.getDatasetHandle().equals(name)) return ds;
		}
		return null;
	}

	private class Handler {
		final String hostname;
		final Dataset ds;
		final InstanceMapReduce mr;
		final ReduceStore<InstanceId, Instance> store;
		final RequestId request;

		public Handler(String hostname, Dataset ds, RequestId request) {
			this.hostname = hostname;
			this.ds = ds;
			this.request = request;

			Context.setDataset(ds);

			String name = String.format("%s:%s", hostname, request);

			mr = new InstanceMapReduce(ds, database);
			store = database.createReducer(Instance.class, mr, name, true);
		}

		public void process(int length, int records, DataInputStream dis)
				throws IOException {

			log.debug("storing {} events", records);
			long started = Calendar.getInstance().getTime().getTime();

			long first = 0, last = 0;

			EventCreator<?> b = database.getEventCreater();

			for (int i = 0; i < records; i++) {
				long id = dis.readLong();
				b.setId(EventId.create(ds, id));
				b.setThread(parseObjectId(ds, dis.readLong()));

				if (i == 0) first = id;
				last = id;

				int type = dis.readInt();

				b.setEvent(type);

				int cnum = dis.readInt();
				int mnum = dis.readInt();

				if ((type & Event.HAS_CLASS) == type) {
					ClazzId clazzId = ClazzId.create(ds, cnum);
					b.setClazz(clazzId);

					if ((type & Event.METHODS) == type) {
						b.setMethod(MethodId.create(ds, clazzId, (short) mnum));
					} else if ((type & Event.FIELDS) == type) {
						b.setField(FieldId.create(ds, clazzId, (short) mnum));
					}
				}

				int len = dis.readInt();

				for (int j = 0; j < len; j++) {
					long arg = dis.readLong();
					b.addArg(parseObjectId(ds, arg));
				}

				mr.map(b.get(), store);
			}

			long finished = Calendar.getInstance().getTime().getTime();
			log.debug("{}: events {} to {} stored successfully in {}ms",
					new Object[]{ds.getDatasetHandle(), first, last, finished - started});
		}

		public void flush() {
			log.debug("flushing instances");
			long started = Calendar.getInstance().getTime().getTime();

			store.flush();

			long finished = Calendar.getInstance().getTime().getTime();
			log.debug("{}: flushed instances in {}ms",
					new Object[]{ds.getDatasetHandle(), finished - started});

			Context.clear();
		}
	}
}
