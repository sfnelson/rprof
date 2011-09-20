package nz.ac.vuw.ecs.rprofs.server;

import com.google.common.annotations.VisibleForTesting;
import nz.ac.vuw.ecs.rprofs.server.context.Context;
import nz.ac.vuw.ecs.rprofs.server.data.ClassManager;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.weaving.ClassParser;
import nz.ac.vuw.ecs.rprofs.server.weaving.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.weaving.Weaver;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * The server side implementation of the RPC service.
 */
@Configurable(autowire = Autowire.BY_TYPE)
public class Weave extends HttpServlet {

	private final org.slf4j.Logger log = LoggerFactory.getLogger(Weave.class);

	@VisibleForTesting
	@Autowired
	ClassManager classes;

	@VisibleForTesting
	@Autowired
	DatasetManager datasets;

	@VisibleForTesting
	@Autowired
	Context context;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		byte[] buffer, result;

		Dataset ds = datasets.findDataset(req.getHeader("Dataset"));
		context.setDataset(ds);

		int length = req.getContentLength();

		log.debug("received class weave request ({}, {})", ds, length);

		buffer = new byte[length];
		InputStream is = req.getInputStream();
		for (int i = 0; i < buffer.length; ) {
			i += is.read(buffer, i, buffer.length - i);
		}

		ClazzId clazzId = new ClassParser(classes.createClazz())
				.read(buffer)
				.store();
		Clazz newClazz = classes.getClazz(clazzId);

		ClassRecord record = new ClassRecord(newClazz);
		record.addFields(classes.findFields(clazzId));
		record.addMethods(classes.findMethods(clazzId));

		result = new Weaver().weave(record, buffer);

		classes.setProperties(clazzId, newClazz.getProperties());

		resp.setStatus(200);
		resp.setContentLength(result.length);
		resp.setContentType("application/rprof");
		resp.getOutputStream().write(result);

		log.debug("class {} woven successfully", newClazz.getName());
		log.debug("returning {} bytes", new Object[]{result.length});

		context.clear();
	}
}
