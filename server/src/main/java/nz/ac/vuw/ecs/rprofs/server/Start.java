package nz.ac.vuw.ecs.rprofs.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.domain.DataSet;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@SuppressWarnings("serial")
@Configurable(autowire=Autowire.BY_TYPE)
public class Start extends HttpServlet {

	@Autowired
	private ContextManager contexts;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		DataSet dataSet = contexts.startRecording();

		resp.addHeader("Dataset", dataSet.getHandle());
		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

}
