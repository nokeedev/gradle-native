package dev.nokee.runtime.base.internal.repositories;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class JettyEmbeddedHttpServer extends AbstractHandler {
	private static final Logger LOGGER = Logger.getLogger(JettyEmbeddedHttpServer.class.getName());
	private final RouteHandler handler;

	public JettyEmbeddedHttpServer(Map<String, RouteHandler> routes) {
		this.handler = new ContentHashingHandler(
			new RoutingHandler(routes)
		);
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			String path = request.getRequestURI();
			LOGGER.info("Received " + request.getMethod() + " " + path);
			Optional<Response> result = handler.handle(path);
			if (result.isPresent()) {
				response.setContentType(result.get().getContentType());
				response.setStatus(HttpServletResponse.SC_OK);
				PrintWriter out = response.getWriter();
				out.write(result.get().getContent());
				out.flush();
				return;
			}

			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
		} catch (Throwable e) {
			LOGGER.info("An exception occurred during the dispatch of the request: " + e.getMessage());
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
		} finally {
			baseRequest.setHandled(true);
			LOGGER.info("Finish dispatching with " + response.getStatus());
		}
	}
}
