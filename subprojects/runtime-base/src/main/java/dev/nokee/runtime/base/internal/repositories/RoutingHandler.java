package dev.nokee.runtime.base.internal.repositories;

import java.util.Map;
import java.util.Optional;

public class RoutingHandler implements RouteHandler {
	private final Map<String, RouteHandler> handlers;

	public RoutingHandler(Map<String, RouteHandler> handlers) {
		this.handlers = handlers;
	}

	@Override
	public Optional<Response> handle(String target) {
		return handlers.entrySet().stream().filter(it -> target.startsWith(it.getKey())).findFirst().flatMap(it -> it.getValue().handle(target));
	}
}
