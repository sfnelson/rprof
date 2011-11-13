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
public class Start extends HttpServlet {

	private final org.slf4j.Logger log = LoggerFactory.getLogger(Start.class);

	private final DatasetManager datasets;

	@Inject
	Start(DatasetManager datasets) {
		this.datasets = datasets;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Dataset dataset = datasets.createDataset();

		log.info("profiler run started");

		resp.addHeader("Dataset", dataset.getHandle());
		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

}
