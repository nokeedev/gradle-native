package dev.nokee.platform.nativebase.internal.repositories;

import java.util.Map;
import java.util.Optional;

public class ContextAwareHandler implements Handler {
	private final Map<String, Handler> handlers;

	public ContextAwareHandler(Map<String, Handler> handlers) {
		this.handlers = handlers;
	}

	@Override
	public Optional<String> handle(String target) {
		return handlers.entrySet().stream().filter(it -> target.startsWith(it.getKey())).findFirst().flatMap(it -> it.getValue().handle(target));
	}
}
