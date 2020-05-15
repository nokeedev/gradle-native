package dev.nokee.runtime.base.internal.repositories;

import java.util.Optional;

public interface RouteHandler {
	Optional<Response> handle(String target);
}
