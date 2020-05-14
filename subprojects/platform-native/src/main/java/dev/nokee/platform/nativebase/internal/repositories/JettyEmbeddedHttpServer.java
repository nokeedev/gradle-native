package dev.nokee.platform.nativebase.internal.repositories;

import com.google.common.collect.ImmutableMap;
import dev.nokee.platform.nativebase.internal.locators.XcRunLocator;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.gradle.api.provider.Provider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

public class JettyEmbeddedHttpServer extends AbstractHandler {
	private static final Logger LOGGER = Logger.getLogger(JettyEmbeddedHttpServer.class.getName());
	private final Handler handler;

	public JettyEmbeddedHttpServer(XcRunLocator xcRunLocator) {
		this.handler = new ContentHashingHandler(
			new ContextAwareHandler(ImmutableMap.<String, Handler>builder()
				.put(FrameworkHandler.CONTEXT_PATH, new FrameworkHandler(xcRunLocator))
				.build())
		);
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			String path = request.getRequestURI();
			LOGGER.info("Received " + request.getMethod() + " " + path);
			Optional<String> result = handler.handle(path);
			if (result.isPresent()) {
				response.setContentType("text/plain; charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);
				PrintWriter out = response.getWriter();
				out.write(result.get());
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
