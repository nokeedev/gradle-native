package dev.nokee.model.testers;

import dev.nokee.model.DomainObjectProvider;

public interface TestProviderGenerator<T> {
	SampleProviders<T> samples();

	DomainObjectProvider<T> create();

	Class<T> getType();
}
