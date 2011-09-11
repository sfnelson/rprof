package nz.ac.vuw.ecs.rprofs.server;

import com.google.common.annotations.VisibleForTesting;
import nz.ac.vuw.ecs.rprofs.server.context.Context;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.StringTokenizer;

public class GwtRequest implements Filter {

	private final org.slf4j.Logger log = LoggerFactory.getLogger(GwtRequest.class);

	@VisibleForTesting
	@Autowired(required = true)
	DatasetManager datasets;

	@VisibleForTesting
	@Autowired(required = true)
	Context context;

	@Override
	public void init(FilterConfig config) throws ServletException {
		// nothing to do
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain)
			throws IOException, ServletException {

		String handle = getDataset(req);
		if (handle != null) {
			Dataset dataset = datasets.findDataset(handle);
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

	private String getDataset(ServletRequest req) {
		StringTokenizer tokenizer = new StringTokenizer(((HttpServletRequest) req).getRequestURI(), "/");
		String dataset = null;
		while (tokenizer.hasMoreTokens()) {
			dataset = tokenizer.nextToken();
		}
		if ("gwtRequest".equals(dataset)) {
			dataset = null;
		}
		return dataset;
	}

	@Override
	public void destroy() {
		// nothing to do

	}
}
