package dev.nokee.model.core;

import org.gradle.api.NamedDomainObjectProvider;

public interface ModelProjectionSpec {
	interface Builder {
		Builder type(Class<?> type);

		Builder forProvider(NamedDomainObjectProvider<?> domainObjectProvider);

		Builder forInstance(Object instance);
	}
}
