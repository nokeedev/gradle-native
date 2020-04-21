package dev.nokee.platform.nativebase.internal.repositories;

import dev.nokee.platform.nativebase.internal.locators.XcRunLocator;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class JettyEmbeddedHttpServer extends AbstractHandler {
	private static final Logger LOGGER = Logger.getLogger(JettyEmbeddedHttpServer.class.getName());
	private final List<FrameworkResolver> resolvers = new ArrayList<>();

	public JettyEmbeddedHttpServer(XcRunLocator xcRunLocator) {
		resolvers.add(new XcRunFrameworkResolver(xcRunLocator));
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			String path = request.getRequestURI();
			LOGGER.info("Received " + request.getMethod() + " " + path);
			if (!path.startsWith("/dev/nokee/framework/")) {
				LOGGER.info("The requested path doesn't match the magic value, make sure you are requesting group 'dev.nokee.framework'.");
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
				return;
			}
			path = path.substring(21);
			for (FrameworkResolver resolver : resolvers) {
				byte[] result = resolver.resolve(path);
				if (result != null) {
					String s = new String(result);// TODO: remove this ping-pong convertion
					response.setContentType("text/plain; charset=utf-8");
					response.setStatus(HttpServletResponse.SC_OK);
					PrintWriter out = response.getWriter();
					out.write(s);
					out.flush();
					return;
				}
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
