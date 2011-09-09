package nz.ac.vuw.ecs.rprofs.server;

import com.google.common.annotations.VisibleForTesting;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;

@SuppressWarnings("serial")
@Configurable(autowire = Autowire.BY_TYPE)
public class Stop extends HttpServlet {

	private final org.slf4j.Logger log = LoggerFactory.getLogger(Stop.class);

	@VisibleForTesting
	@Autowired
	Database database;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Dataset dataset = database.getDataset(req.getHeader("Dataset"));
		Calendar now = Calendar.getInstance();
		log.info("profiler run stopped at {}", now.getTime());

		database.setStopped(dataset, now.getTime());

		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

}
