package dev.nokee.model.internal;

import lombok.EqualsAndHashCode;
import org.gradle.api.provider.Provider;

@EqualsAndHashCode
public final class RealizableGradleProvider implements RealizableDomainObject {
	private final Provider<?> provider;

	public RealizableGradleProvider(Provider<?> provider) {
		assert provider != null;
		this.provider = provider;
	}

	@Override
	public void realize() {
		provider.get();
	}
}
