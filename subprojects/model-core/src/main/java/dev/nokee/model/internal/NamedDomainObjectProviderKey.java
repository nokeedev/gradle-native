package dev.nokee.model.internal;

import lombok.EqualsAndHashCode;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.internal.provider.ProviderInternal;

@EqualsAndHashCode
final class NamedDomainObjectProviderKey {
	public static NamedDomainObjectProviderKey of(NamedDomainObjectProvider<?> delegate) {
		return new NamedDomainObjectProviderKey(delegate.getName(), ((ProviderInternal) delegate).getType());
	}

	private final String name;
	private final Class<?> type;

	private NamedDomainObjectProviderKey(String name, Class<?> type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return type;
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", name, type.getSimpleName());
	}
}
