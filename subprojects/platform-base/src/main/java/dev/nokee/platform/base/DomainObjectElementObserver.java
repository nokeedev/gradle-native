package dev.nokee.platform.base;

import org.gradle.api.Action;

public interface DomainObjectElementObserver<T> {
	void whenElementKnown(Action<KnownDomainObject<? extends T>> action);
	<U> void whenElementKnown(Class<U> type, Action<KnownDomainObject<? extends U>> action);
}
