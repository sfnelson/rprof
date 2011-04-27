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

import nz.ac.vuw.ecs.rprofs.server.context.Context;
import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;

public class GwtRequest implements Filter {

	private ContextManager cm;

	@Override
	public void init(FilterConfig fc) throws ServletException {
		cm = ContextManager.getInstance();
	}

	@Override
	public void destroy() {
		cm = null;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain)
	throws IOException, ServletException {

		Context c = cm.setCurrent(getDataset(req));

		try {
			c.open();
			chain.doFilter(req, rsp);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			c.clear();
		}
		finally {
			c.close();
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

}
