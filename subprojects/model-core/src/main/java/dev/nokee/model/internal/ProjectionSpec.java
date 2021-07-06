package dev.nokee.model.internal;

import dev.nokee.utils.ProviderUtils;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.provider.Provider;

final class ProjectionSpec {
	private final Class<?> type;
	private final ConfigurationStrategy configurationStrategy;
	private final Provider<?> provider;

	private ProjectionSpec(Class<?> type, ConfigurationStrategy configurationStrategy, Provider<?> provider) {
		this.type = type;
		this.configurationStrategy = configurationStrategy;
		this.provider = provider;
	}

	public <T> void configure(Action<? super T> action) {
		configurationStrategy.configure(action);
	}

	public boolean canBeViewedAs(Class<?> type) {
		return type.isAssignableFrom(this.type);
	}

	public <T> T get(Class<T> type) {
		if (type.isAssignableFrom(Provider.class)) {
			return type.cast(provider);
		}
		return type.cast(provider.get());
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private Class<?> type;
		private Provider<?> provider;
		private ConfigurationStrategy configurationStrategy;

		public Builder type(Class<?> type) {
			this.type = type;
			return this;
		}

		public Builder forProvider(NamedDomainObjectProvider<?> provider) {
			this.configurationStrategy = new ProvidedConfigurationStrategy(provider);
			this.provider = provider;
			return this;
		}

		public Builder forInstance(Object instance) {
			this.configurationStrategy = new ExistingConfigurationStrategy(instance);
			this.provider = ProviderUtils.fixed(instance);
			return this;
		}

		public ProjectionSpec build() {
			if (type == null) {
				type = ProviderUtils.getType(provider).orElse(null);
			}
			ProviderUtils.getType(provider).ifPresent(type -> {
				if (!this.type.isAssignableFrom(type)) {
					throw new RuntimeException();
				}
			});
			return new ProjectionSpec(type, configurationStrategy, provider);
		}
	}

	private interface ConfigurationStrategy {
		<T> void configure(Action<? super T> action);
	}

	private static final class ExistingConfigurationStrategy implements ConfigurationStrategy {
		private final Object target;

		private ExistingConfigurationStrategy(Object target) {
			this.target = target;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> void configure(Action<? super T> action) {
			action.execute((T) target);
		}
	}

	private static final class ProvidedConfigurationStrategy implements ConfigurationStrategy {
		private final NamedDomainObjectProvider<?> target;

		private ProvidedConfigurationStrategy(NamedDomainObjectProvider<?> target) {
			this.target = target;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> void configure(Action<? super T> action) {
			((NamedDomainObjectProvider<T>) target).configure(action);
		}
	}
}
