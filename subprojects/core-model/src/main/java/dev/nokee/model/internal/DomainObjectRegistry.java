package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectProvider;
import org.gradle.api.Action;

public interface DomainObjectRegistry<T> {
	<S extends T> DomainObjectProvider<S> register(String name, Class<S> type);
	<S extends T> DomainObjectProvider<S> register(String name, Class<S> type, Action<? super S> action);
}
