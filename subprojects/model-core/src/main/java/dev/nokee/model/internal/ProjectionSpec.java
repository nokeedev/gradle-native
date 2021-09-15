package dev.nokee.model.internal;

import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelProjection;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import lombok.var;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal;
import org.gradle.api.internal.provider.ProviderInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
		configurationStrategy.realize();
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
			} else if (configurationStrategy instanceof ConfigurationProvidedConfigurationStrategy) {
				return type.cast(((ConfigurationProvidedConfigurationStrategy) configurationStrategy).target);
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
		private static final NamedDomainObjectProviderSelfMutationDecoratorFactory DECORATOR_FACTORY = new DefaultNamedDomainObjectSelfMutationDecoratorFactory();
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
			if (Configuration.class.isAssignableFrom(((ProviderInternal<?>)provider).getType())) {
				this.configurationStrategy = new ConfigurationProvidedConfigurationStrategy(provider);
			} else {
				this.configurationStrategy = new ProvidedConfigurationStrategy(provider);
			}
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
					val provider = registry.registerIfAbsent(calculateName(), type);
					val container = ((NamedDomainObjectRegistryInternal) registry).registry(type).getContainer();
					forProvider(DECORATOR_FACTORY.forContainer(container).decorate(provider));
				} else if (objectFactory != null) {
					forInstance(objectFactory.newInstance(type));
				}
			}
			ProviderUtils.getType(provider).ifPresent(type -> {
				if (!this.type.isAssignableFrom(type)) {
					throw new RuntimeException();
				}
			});

			if (ownedBy != null) {
				configurationStrategy.onRealize(() -> {
					realize(ownedBy);
				});
			}

			return new ProjectionSpec(type, configurationStrategy, provider);
		}

		private void realize(ModelNode node) {
			node.getParent().ifPresent(this::realize);
			node.getProjections().forEach(ModelProjection::realize);
		}

		private String calculateName() {
			var previous = ownedBy.getParent();
			String name = "";
			while (previous.isPresent()) {
				val n = name;
				name = maybeNameOf(previous.get().getIdentity()).map(it -> it + StringUtils.capitalize(n)).orElse(name);
				previous = previous.get().getParent();
			}

			if (Task.class.isAssignableFrom(type)) {
				name = nameOf(ownedBy.getIdentity()) + StringUtils.capitalize(name);
			} else if (name.isEmpty()) {
				name = nameOf(ownedBy.getIdentity());
			} else {
				name = name + StringUtils.capitalize(nameOf(ownedBy.getIdentity()));
			}

			return name;
		}
	}

	private static String nameOf(Object identity) {
		if (identity instanceof Named) {
			return ((Named) identity).getName();
		} else {
			return identity.toString();
		}
	}

	private static Optional<String> maybeNameOf(Object identity) {
		if (identity instanceof NameProvider) {
			return ((NameProvider) identity).getProvidedName();
		}
		return Optional.of(nameOf(identity)).filter(it -> !it.isEmpty());
	}

	private interface ConfigurationStrategy {
		<T> void configure(Action<? super T> action);

		void realize();
		void onRealize(Runnable callback);
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

		@Override
		public void realize() {
			// do nothing...
		}

		@Override
		public void onRealize(Runnable callback) {
			// do nothing...
		}

		@Override
		public String toString() {
			return target.toString();
		}
	}

	private static final class ProvidedConfigurationStrategy implements ConfigurationStrategy {
		private final NamedDomainObjectProvider<?> target;
		private final MutableBoolean realized = new MutableBoolean(false);
		private Runnable onRealize = () -> {};

		private ProvidedConfigurationStrategy(NamedDomainObjectProvider<?> target) {
			this.target = target;
			target.configure(it -> {
				realized.setTrue();
				onRealize.run();
			});
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> void configure(Action<? super T> action) {
			((NamedDomainObjectProvider<T>) target).configure(action);
		}

		@Override
		public void realize() {
			target.get();
		}

		@Override
		public void onRealize(Runnable callback) {
			onRealize = callback;
		}
	}

	private static final class ConfigurationProvidedConfigurationStrategy implements ConfigurationStrategy {
		private final NamedDomainObjectProvider<?> target;
		private final MutableBoolean realized = new MutableBoolean(false);
		private Runnable onRealize = () -> {};
		private final ArrayDeque<Action> actions = new ArrayDeque<>();

		private ConfigurationProvidedConfigurationStrategy(NamedDomainObjectProvider<?> target) {
			this.target = target;
			target.configure(it -> {
				((ConfigurationInternal) it).beforeLocking(c -> {
					if (!realized.booleanValue()) {
						realized.setTrue();
						onRealize.run();
						while (!actions.isEmpty()) {
							val action = actions.removeFirst();
							target.configure(action);
						}
					}
				});
			});
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> void configure(Action<? super T> action) {
			if (realized.booleanValue() && actions.isEmpty()) {
				((NamedDomainObjectProvider<T>) target).configure(action);
			} else {
				actions.add(action);
			}
		}

		@Override
		public void realize() {
			if (realized.isFalse()) {
				target.get();
				realized.setTrue();
				onRealize.run();
				while (!actions.isEmpty()) {
					val action = actions.removeFirst();
					target.configure(action);
				}
			}
		}

		@Override
		public void onRealize(Runnable callback) {
			onRealize = callback;
		}
	}
}
