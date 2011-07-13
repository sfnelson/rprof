package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.request.DatasetService;

import org.springframework.beans.factory.annotation.Autowired;

public class GwtRequest implements Filter {

	private final java.util.logging.Logger log = java.util.logging.Logger.getLogger("gwt-request");

	@Autowired
	private DatasetService datasets;

	@Override
	public void init(FilterConfig config) throws ServletException {
		// nothing to do
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain)
			throws IOException, ServletException {

		ContextManager.setThreadLocal(datasets.findDataset(getDataset(req)));

		try {
			chain.doFilter(req, rsp);
		}
		catch (Exception ex) {
			log.log(Level.SEVERE, ex.getMessage(), ex);
		}
		finally {
			ContextManager.setThreadLocal(null);
		}
	}

	private String getDataset(ServletRequest req) {
		StringTokenizer tokenizer = new StringTokenizer(((HttpServletRequest) req).getRequestURI(), "/");
		String dataset = null;
		while (tokenizer.hasMoreTokens()) {
			dataset = tokenizer.nextToken();
		}
		if (dataset.equals("gwtRequest")) {
			dataset = null;
		}
		return dataset;
	}

	@Override
	public void destroy() {
		// nothing to do

	}
}
