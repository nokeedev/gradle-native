package dev.nokee.platform.base.internal.components;

import com.google.common.base.Preconditions;
import dev.nokee.model.DomainObjectFactory;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.*;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import lombok.val;
import org.gradle.api.Action;

public final class ComponentContainerImpl extends AbstractDomainObjectContainer<Component, Component> implements ComponentContainerInternal {
	private final ProjectIdentifier owner;

	public ComponentContainerImpl(ProjectIdentifier owner, ComponentConfigurer configurer, DomainObjectEventPublisher eventPublisher, ComponentProviderFactory providerFactory, ComponentRepository repository, KnownComponentFactory knownComponentFactory, ComponentInstantiator instantiator) {
		super(owner, Component.class, instantiator, new ComponentOnlyConfigurer(configurer), eventPublisher, providerFactory, repository, knownComponentFactory);
		this.owner = owner;
	}

	@Override
	public ComponentContainerImpl disallowChanges() {
		super.disallowChanges();
		return this;
	}

	@Override
	public <U extends Component> void registerFactory(Class<U> type, DomainObjectFactory<? extends U> factory) {
		Preconditions.checkArgument(!isTestSuiteComponent(type), "Cannot register test suite component types in this container, use a TestSuiteContainer instead.");
		super.registerFactory(type, factory);
	}

	@Override
	public <U extends Component> void registerBinding(Class<U> type, Class<? extends U> implementationType) {
		Preconditions.checkArgument(!isTestSuiteComponent(type), "Cannot bind test suite component types in this container, use a TestSuiteContainer instead.");
		Preconditions.checkArgument(!isTestSuiteComponent(implementationType), "Cannot bind to test suite component types in this container, use a TestSuiteContainer instead.");
		super.registerBinding(type, implementationType);
	}

	@Override
	protected <U extends Component> TypeAwareDomainObjectIdentifier<U> newIdentifier(String name, Class<U> type) {
		Preconditions.checkArgument(!isTestSuiteComponent(type), "Cannot register test suite components in this container, use a TestSuiteContainer instead.");
		return ComponentIdentifier.of(ComponentName.of(name), type, owner);
	}

	@Override
	public void whenElementKnown(Action<? super KnownComponent<Component>> action) {
		doWhenElementKnown(Component.class, (Action<? super KnownDomainObject<Component>>)action);
	}

	@Override
	public <T extends Component> void whenElementKnown(Class<T> type, Action<? super KnownComponent<T>> action) {
		doWhenElementKnown(type, (Action<? super KnownDomainObject<T>>)action);
	}

	private static boolean isTestSuiteComponent(Component entity) {
		try {
			val TestSuiteComponent = Class.forName("dev.nokee.testing.base.TestSuiteComponent");
			return TestSuiteComponent.isInstance(entity);
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	private static boolean isTestSuiteComponent(Class<? extends Component> entityType) {
		try {
			val TestSuiteComponent = Class.forName("dev.nokee.testing.base.TestSuiteComponent");
			return TestSuiteComponent.isAssignableFrom(entityType);
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	private static final class ComponentOnlyConfigurer implements DomainObjectConfigurer<Component> {
		private final DomainObjectConfigurer<Component> configurer;

		ComponentOnlyConfigurer(DomainObjectConfigurer<Component> configurer) {
			this.configurer = configurer;
		}

		@Override
		public <S extends Component> void configureEach(DomainObjectIdentifier owner, Class<S> type, Action<? super S> action) {
			Preconditions.checkArgument(!isTestSuiteComponent(type), "Cannot configure test suite components in this container, use a TestSuiteContainer instead.");
			configurer.configureEach(owner, type, wrapConfigureAction(action));
		}

		@Override
		public <S extends Component> void configure(DomainObjectIdentifier owner, String name, Class<S> type, Action<? super S> action) {
			Preconditions.checkArgument(!isTestSuiteComponent(type), "Cannot configure test suite components in this container, use a TestSuiteContainer instead.");
			configurer.configure(owner, name, type, wrapConfigureAction(action));
		}

		@Override
		public <S extends Component> void configure(TypeAwareDomainObjectIdentifier<S> identifier, Action<? super S> action) {
			Preconditions.checkArgument(!isTestSuiteComponent(identifier.getType()), "Cannot configure test suite components in this container, use a TestSuiteContainer instead.");
			configurer.configure(identifier, action);
		}

		@Override
		public <S extends Component> void whenElementKnown(DomainObjectIdentifier owner, Class<S> type, Action<? super TypeAwareDomainObjectIdentifier<S>> action) {
			Preconditions.checkArgument(!isTestSuiteComponent(type), "Cannot configure test suite components in this container, use a TestSuiteContainer instead.");
			configurer.whenElementKnown(owner, type, wrapObserveAction(action));
		}

		private static <U extends Component> Action<? super U> wrapConfigureAction(Action<? super U> action) {
			return entity -> {
				if (!isTestSuiteComponent(entity)) {
					action.execute(entity);
				}
			};
		}

		private static <U extends Component> Action<? super TypeAwareDomainObjectIdentifier<U>> wrapObserveAction(Action<? super TypeAwareDomainObjectIdentifier<U>> action) {
			return identifier -> {
				if (!isTestSuiteComponent(identifier.getType())) {
					action.execute(identifier);
				}
			};
		}
	}
}
