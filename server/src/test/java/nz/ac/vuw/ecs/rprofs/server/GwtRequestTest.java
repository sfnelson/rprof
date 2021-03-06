package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nz.ac.vuw.ecs.rprofs.Context;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.fail;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 12/09/11
 */
public class GwtRequestTest {

	GwtRequest servlet;

	DatasetManager datasets;
	Context context;

	FilterConfig config;
	HttpServletRequest req;
	HttpServletResponse rsp;
	FilterChain chain;

	@Before
	public void setUp() throws Exception {
		config = createMock(FilterConfig.class);
		req = createMock(HttpServletRequest.class);
		rsp = createMock(HttpServletResponse.class);
		chain = createMock(FilterChain.class);

		datasets = createMock(DatasetManager.class);
		context = createMock(Context.class);

		servlet = new GwtRequest(datasets, context);
	}

	@Test
	public void testInit() throws Exception {
		replay(datasets, context, config);
		servlet.init(config);
		verify(datasets, context, config);
	}

	@Test
	public void testDoFilter() throws Exception {
		DatasetId id = new DatasetId((short) 1);
		Dataset dataset = new Dataset(id, "program", new Date(), "rprof_program_1");

		expect(req.getRequestURI()).andReturn("/gwtRequest/1");
		expect(datasets.findDataset(id)).andReturn(dataset);
		context.setDataset(dataset);
		chain.doFilter(req, rsp);
		context.clear();

		replay(datasets, context, req, rsp, chain);

		servlet.doFilter(req, rsp, chain);

		verify(datasets, context, req, rsp, chain);
	}

	@Test
	public void testDoFilterRuntimeException() throws Exception {
		DatasetId id = new DatasetId((short) 1);
		Dataset dataset = null;

		expect(req.getRequestURI()).andReturn("/gwtRequest");
		chain.doFilter(req, rsp);
		expectLastCall().andThrow(new RuntimeException("foobar"));
		context.clear();

		replay(datasets, context, req, rsp, chain);

		try {
			servlet.doFilter(req, rsp, chain);
		} catch (RuntimeException ex) {
			verify(datasets, context, req, rsp, chain);
			return;
		}

		fail("should not return normally");
	}

	@Test
	public void testDoFilterIOException() throws Exception {
		DatasetId id = new DatasetId((short) 1);
		Dataset dataset = null;

		expect(req.getRequestURI()).andReturn("/gwtRequest");
		chain.doFilter(req, rsp);
		expectLastCall().andThrow(new IOException("foobar"));
		context.clear();

		replay(datasets, context, req, rsp, chain);

		try {
			servlet.doFilter(req, rsp, chain);
		} catch (IOException ex) {
			verify(datasets, context, req, rsp, chain);
			return;
		}

		fail("should not return normally");
	}

	@Test
	public void testDoFilterServletException() throws Exception {
		DatasetId id = new DatasetId((short) 1);
		Dataset dataset = null;

		expect(req.getRequestURI()).andReturn("/gwtRequest");
		chain.doFilter(req, rsp);
		expectLastCall().andThrow(new ServletException("foobar"));
		context.clear();

		replay(datasets, context, req, rsp, chain);

		try {
			servlet.doFilter(req, rsp, chain);
		} catch (ServletException ex) {
			verify(datasets, context, req, rsp, chain);
			return;
		}

		fail("should not return normally");
	}

	@Test
	public void testDestroy() throws Exception {
		replay(datasets, context);
		servlet.destroy();
		verify(datasets, context);
	}
}
