package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;
import java.util.StringTokenizer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import nz.ac.vuw.ecs.rprofs.Context;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;
import org.slf4j.LoggerFactory;

@Singleton
public class GwtRequest implements Filter {

	private final org.slf4j.Logger log = LoggerFactory.getLogger(GwtRequest.class);

	private final DatasetManager datasets;
	private final Context context;

	@Inject
	GwtRequest(DatasetManager datasets, Context context) {
		this.datasets = datasets;
		this.context = context;
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		// nothing to do
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain)
			throws IOException, ServletException {

		DatasetId id = getDataset(req);
		if (id != null) {
			Dataset dataset = datasets.findDataset(id);
			if (dataset != null) {
				context.setDataset(dataset);
			}
		}

		try {
			chain.doFilter(req, rsp);
		} catch (RuntimeException ex) {
			log.error(ex.getMessage(), ex);
			throw ex;
		} catch (IOException ex) {
			log.error(ex.getMessage(), ex);
			throw ex;
		} catch (ServletException ex) {
			log.error(ex.getMessage(), ex);
			throw ex;
		} finally {
			context.clear();
		}
	}

	private DatasetId getDataset(ServletRequest req) {
		StringTokenizer tokenizer = new StringTokenizer(((HttpServletRequest) req).getRequestURI(), "/");
		String dataset = null;
		while (tokenizer.hasMoreTokens()) {
			dataset = tokenizer.nextToken();
		}
		if (dataset == null || !dataset.matches("^[0-9]*$")) {
			return null;
		} else {
			return new DatasetId(Long.parseLong(dataset));
		}
	}

	@Override
	public void destroy() {
		// nothing to do

	}
}
