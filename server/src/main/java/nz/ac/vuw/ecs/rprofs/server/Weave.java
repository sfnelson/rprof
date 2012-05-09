package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;
import java.io.InputStream;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nz.ac.vuw.ecs.rprofs.Context;
import nz.ac.vuw.ecs.rprofs.server.data.ClassManager;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.weaving.ClassParser;
import nz.ac.vuw.ecs.rprofs.server.weaving.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.weaving.Weaver;
import org.slf4j.LoggerFactory;

@Singleton
public class Weave extends HttpServlet {

	private final org.slf4j.Logger log = LoggerFactory.getLogger(Weave.class);

	private final ClassManager classes;
	private final DatasetManager datasets;
	private final Context context;

	@Inject
	Weave(ClassManager classes, DatasetManager datasets, Context context) {
		this.classes = classes;
		this.datasets = datasets;
		this.context = context;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		byte[] buffer, result;

		Dataset ds = datasets.findDataset(req.getHeader("Dataset"));
		context.setDataset(ds);

		String cname = req.getParameter("cls");

		int length = req.getContentLength();

		log.trace("received class weave request: {} ({} bytes)", cname, length);

		buffer = new byte[length];
		InputStream is = req.getInputStream();
		for (int i = 0; i < buffer.length; ) {
			i += is.read(buffer, i, buffer.length - i);
		}

		ClazzId clazzId = new ClassParser(classes.createClazz())
				.read(buffer)
				.storeIfNotInterface();
		Clazz newClazz = null;

		if (clazzId == null) {
			result = null;
			log.debug("interface {} (ignored)", cname);
		} else {
			newClazz = classes.getClazz(clazzId);

			ClassRecord record = new ClassRecord(newClazz);
			record.addFields(classes.findFields(clazzId));
			record.addMethods(classes.findMethods(clazzId));

			result = new Weaver().weave(record, buffer);

			classes.setProperties(clazzId, newClazz.getProperties());
			resp.setHeader("class-id", String.valueOf(newClazz.getId().getClassIndex()));
			resp.setHeader("properties", String.valueOf(newClazz.getProperties()));

			if (result != null) {
				log.debug("class {} woven successfully ({})", newClazz.getName(), clazzId.getClassIndex());
			} else {
				log.debug("class {} tagged but not woven (excluded) ({})", newClazz.getName(), clazzId.getClassIndex());
			}
		}

		if (result != null) {
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentLength(result.length);
			resp.getOutputStream().write(result);
		} else {
			resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
			resp.setContentLength(0);
			resp.getOutputStream().close();
		}
		resp.setContentType("application/rprof");

		context.clear();
	}
}
