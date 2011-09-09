package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.db.Database;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class GwtRequest implements Filter {

	private final org.slf4j.Logger log = LoggerFactory.getLogger(GwtRequest.class);

	@Autowired
	private Database database;

	@Override
	public void init(FilterConfig config) throws ServletException {
		// nothing to do
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain)
			throws IOException, ServletException {

		ContextManager.setThreadLocal(database.getDataset(getDataset(req)));

		try {
			chain.doFilter(req, rsp);
		}
		catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		finally {
			ContextManager.clearThreadLocal();
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
