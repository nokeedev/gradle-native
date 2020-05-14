package dev.nokee.platform.nativebase.internal.repositories;

import java.util.Optional;

public interface Handler {
	Optional<String> handle(String target);
}
