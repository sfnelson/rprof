package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import org.slf4j.LoggerFactory;

@Singleton
public class Stop extends HttpServlet {

	private final org.slf4j.Logger log = LoggerFactory.getLogger(Stop.class);

	private final DatasetManager datasets;

	@Inject
	Stop(DatasetManager datasets) {
		this.datasets = datasets;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String handle = req.getHeader("Dataset");

		Dataset dataset = datasets.findDataset(handle);
		datasets.stopDataset(dataset.getId());

		Dataset ds = datasets.findDataset(handle);

		log.info("profiler run stopped");

		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
		resp.setContentLength(0);
	}

}
