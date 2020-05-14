package dev.nokee.platform.nativebase.internal.repositories;

import java.util.Optional;

public interface Handler {
	Optional<Response> handle(String target);
}
