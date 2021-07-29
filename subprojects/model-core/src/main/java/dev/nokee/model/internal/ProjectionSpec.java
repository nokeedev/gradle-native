package dev.nokee.model.internal;

import dev.nokee.model.core.ModelNode;
import dev.nokee.utils.ProviderUtils;
import lombok.var;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

final class ProjectionSpec {
	private final Class<?> type;
	private final ConfigurationStrategy configurationStrategy;
	private final Provider<?> provider;
	private final List<Action<?>> finalizeActions = new ArrayList<>();
	private boolean finalized = false;
	private boolean realizeOnFinalize = false;

	private ProjectionSpec(Class<?> type, ConfigurationStrategy configurationStrategy, Provider<?> provider) {
		this.type = type;
		this.configurationStrategy = configurationStrategy;
		this.provider = provider;
	}

	public <T> void configure(Action<? super T> action) {
		configurationStrategy.configure(action);
	}

	public <T> void finalize(Action<? super T> action) {
		if (finalized) {
			configurationStrategy.configure(action);
		} else {
			finalizeActions.add(action);
		}
	}

	public void realizeProjection() {
		provider.get();
	}

	public void finalizeProjection() {
		finalizeActions.forEach(it -> configure((Action<? super Object>) it));
		finalizeActions.clear();

		if (realizeOnFinalize) {
			realizeProjection();
		}
		finalized = true;
	}

	public void realizeOnFinalize() {
		realizeOnFinalize = true;
		if (finalized) {
			realizeProjection();
		}
	}

	public boolean canBeViewedAs(Class<?> type) {
		if (Provider.class.isAssignableFrom(type)) {
			return true;
		}
		return type.isAssignableFrom(this.type);
	}

	public <T> T get(Class<T> type) {
		if (Provider.class.isAssignableFrom(type)) {
			if (configurationStrategy instanceof ProvidedConfigurationStrategy) {
				return type.cast(((ProvidedConfigurationStrategy) configurationStrategy).target);
			} else if (configurationStrategy instanceof ExistingConfigurationStrategy) {
				return type.cast(provider);
			}
		}
		return type.cast(provider.get());
	}

	public Class<?> getType() {
		return type;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private Class<?> type;
		private Provider<?> provider;
		private ConfigurationStrategy configurationStrategy;
		@Nullable private NamedDomainObjectRegistry registry;
		private ModelNode ownedBy;
		@Nullable private ObjectFactory objectFactory;

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

		private Class<?> toUndecoratedType(Class<?> type) {
			if (type.getSimpleName().endsWith("_Decorated")) {
				if (type.getSuperclass().equals(Object.class)) {
					return type.getInterfaces()[0];
				} else {
					return type.getSuperclass();
				}
			}
			return type;
		}

		public Builder registry(@Nullable NamedDomainObjectRegistry registry) {
			this.registry = registry;
			return this;
		}

		public Builder objectFactory(@Nullable ObjectFactory objectFactory) {
			this.objectFactory = objectFactory;
			return this;
		}

		public Builder ownedBy(ModelNode ownedBy) {
			this.ownedBy = ownedBy;
			return this;
		}

		public ProjectionSpec build() {
			if (type == null) {
				type = ProviderUtils.getType(provider).map(this::toUndecoratedType).orElse(null);
			} else if (provider == null) {
				if (registry != null && ownedBy != null && registry.getRegistrableTypes().canRegisterType(type)) {
					forProvider(registry.registerIfAbsent(calculateName(), type));
				} else if (objectFactory != null) {
					forInstance(objectFactory.newInstance(type));
				}
			}
			ProviderUtils.getType(provider).ifPresent(type -> {
				if (!this.type.isAssignableFrom(type)) {
					throw new RuntimeException();
				}
			});
			return new ProjectionSpec(type, configurationStrategy, provider);
		}

		private String calculateName() {
			var previous = ownedBy.getParent();
			String name = "";
			while (previous.isPresent()) {
				name = maybeNameOf(previous.get().getIdentity()) + StringUtils.capitalize(name);
				previous = previous.get().getParent();
			}

			if (Task.class.isAssignableFrom(type)) {
				name = nameOf(ownedBy.getIdentity()) + StringUtils.capitalize(name);
			} else {
				name = name + StringUtils.capitalize(nameOf(ownedBy.getIdentity()));
			}

			return StringUtils.uncapitalize(name);
		}
	}

	private static String nameOf(Object identity) {
		if (identity instanceof Named) {
			return ((Named) identity).getName();
		} else {
			return identity.toString();
		}
	}

	// TODO:
	private static String maybeNameOf(Object identity) {
		if (identity instanceof NameProvider) {
			return ((NameProvider) identity).getProvidedName().orElse("");
		}
		return nameOf(identity);
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
