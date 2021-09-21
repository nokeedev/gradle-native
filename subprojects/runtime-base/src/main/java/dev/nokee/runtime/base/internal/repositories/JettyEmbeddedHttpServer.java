/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
