package dev.nokee.model.internal;

import dev.nokee.model.NokeeExtension;
import dev.nokee.model.dsl.ModelNode;
import dev.nokee.model.registry.ModelRegistry;
import dev.nokee.utils.NamedDomainObjectCollectionUtils;
import dev.nokee.utils.SpecUtils;
import groovy.lang.Closure;
import lombok.Value;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.PolymorphicDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.specs.Spec;
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Supplier;

import static dev.nokee.utils.NamedDomainObjectCollectionUtils.whenElementKnown;

abstract /*final*/ class DefaultNokeeExtension implements NokeeExtension {
	private final ModelRegistry modelRegistry;
	private final ObjectFactory objectFactory;
	private final DefaultNamedDomainObjectRegistry registry;
	private final ModelNode model;

	@Inject
	public DefaultNokeeExtension(DefaultNamedDomainObjectRegistry registry, ObjectFactory objectFactory) {
		this.modelRegistry = new DefaultModelRegistry(objectFactory);
		this.objectFactory = objectFactory;
		this.model = new DefaultModelNodeDslFactory(registry, modelRegistry.allProjections(), objectFactory).create(modelRegistry.getRoot());
		this.registry = registry;
	}

	@Override
	public ModelRegistry getModelRegistry() {
		return modelRegistry;
	}

	@Override
	public <T> DefaultNokeeExtension bridgeContainer(NamedDomainObjectContainer<T> container) {
		val bridgedContainer = new BridgedContainer<>(new NamedDomainObjectContainerRegistry.NamedContainerRegistry<>(container), modelRegistry);
		registry.registerContainer(bridgedContainer);
		whenElementKnown(container, bridgedContainer);
		return this;
	}

	@Override
	public <T> DefaultNokeeExtension bridgeContainer(PolymorphicDomainObjectContainer<T> container) {
		val bridgedContainer = new BridgedContainer<>(new NamedDomainObjectContainerRegistry.PolymorphicContainerRegistry<>(container), modelRegistry);
		registry.registerContainer(bridgedContainer);
		whenElementKnown(container, bridgedContainer);
		return this;
	}

	private static final class BridgedContainer<T> extends NamedDomainObjectContainerRegistry<T> implements Action<NamedDomainObjectCollectionUtils.KnownElement<T>> {
		private static final ThreadLocal<Spec<NamedDomainObjectCollectionUtils.KnownElement<?>>> CURRENT_DOMAIN_OBJECT_SPEC = new ThreadLocal<>();
		private final NamedDomainObjectContainerRegistry<T> delegate;
		private final Action<NamedDomainObjectCollectionUtils.KnownElement<T>> whenElementKnown;

		private BridgedContainer(NamedDomainObjectContainerRegistry<T> delegate, ModelRegistry modelRegistry) {
			this.delegate = delegate;
			this.whenElementKnown = new RegisterModelProjection<>(modelRegistry);
		}

		@Override
		public <S extends T> NamedDomainObjectProvider<S> register(String name, Class<S> type) {
			return doRegister(name, type, () -> delegate.register(name, type));
		}

		@Override
		public <S extends T> NamedDomainObjectProvider<S> register(String name, Class<S> type, Action<? super S> action) {
			return doRegister(name, type, () -> delegate.register(name, type, action));
		}

		@Override
		public <S extends T> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type) {
			return doRegister(name, type, () -> delegate.registerIfAbsent(name, type));
		}

		@Override
		public <S extends T> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type, Action<? super S> action) {
			return doRegister(name, type, () -> delegate.registerIfAbsent(name, type, action));
		}

		private static <T> NamedDomainObjectProvider<T> doRegister(String name, Class<T> type, Supplier<? extends NamedDomainObjectProvider<T>> supplier) {
			val previous = CURRENT_DOMAIN_OBJECT_SPEC.get();
			CURRENT_DOMAIN_OBJECT_SPEC.set(new DomainObjectSpec(name, type));
			try {
				return supplier.get();
			} finally {
				CURRENT_DOMAIN_OBJECT_SPEC.set(previous);
			}
		}

		@Override
		public NamedDomainObjectRegistry.RegistrableTypes getRegistrableTypes() {
			return delegate.getRegistrableTypes();
		}

		@Override
		public NamedDomainObjectContainer<T> getContainer() {
			return delegate.getContainer();
		}

		@Override
		public void execute(NamedDomainObjectCollectionUtils.KnownElement<T> knownElement) {
			doNotExecuteIfPreKnown(knownElement, whenElementKnown);
		}

		private static <T> void doNotExecuteIfPreKnown(NamedDomainObjectCollectionUtils.KnownElement<T> knownElement, Action<NamedDomainObjectCollectionUtils.KnownElement<T>> action) {
			val spec = Optional.ofNullable(CURRENT_DOMAIN_OBJECT_SPEC.get()).orElse(SpecUtils.satisfyNone());
			if (!spec.isSatisfiedBy(knownElement)) {
				action.execute(knownElement);
			}
		}

		@Value
		private static class DomainObjectSpec implements Spec<NamedDomainObjectCollectionUtils.KnownElement<?>> {
			String name;
			Class<?> type;

			@Override
			public boolean isSatisfiedBy(NamedDomainObjectCollectionUtils.KnownElement<?> element) {
				return element.getName().equals(name) && element.getType().equals(type);
			}
		}
	}

	@Override
	public ModelNode getModel() {
		return model;
	}

	@Override
	public void model(Action<? super ModelNode> action) {
		action.execute(model);
	}

	@Override
	public NokeeExtension configure(Action<? super NokeeExtension> action) {
		action.execute(this);
		return this;
	}

	@Override
	public NokeeExtension configure(Class<? extends Action<? super NokeeExtension>> actionClass) {
		objectFactory.newInstance(actionClass).execute(this);
		return this;
	}

	@Override
	public NokeeExtension configure(Closure closure) {
		ConfigureUtil.configureSelf(closure, this);
		return this;
	}
}
