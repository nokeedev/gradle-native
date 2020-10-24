package dev.nokee.testing.base.internal;

import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.*;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.components.*;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.testing.base.TestSuiteContainer;
import org.gradle.api.Action;

import java.util.HashMap;
import java.util.Map;

public final class DefaultTestSuiteContainer extends AbstractDomainObjectContainer<Component, TestSuiteComponent> implements TestSuiteContainer {
	private final Map<Class<? extends TestSuiteComponent>, Class<? extends TestSuiteComponent>> bindings = new HashMap<>();
	private final ProjectIdentifier owner;

	public DefaultTestSuiteContainer(ProjectIdentifier owner, ComponentConfigurer configurer, DomainObjectEventPublisher eventPublisher, ComponentProviderFactory providerFactory, ComponentRepository repository, KnownComponentFactory knownComponentFactory, ComponentInstantiator instantiator) {
		super(owner, TestSuiteComponent.class, instantiator, configurer, eventPublisher, providerFactory, repository, knownComponentFactory);
		this.owner = owner;
	}

	@Override
	public void whenElementKnown(Action<? super KnownComponent<? extends TestSuiteComponent>> action) {
		doWhenElementKnown(TestSuiteComponent.class, (Action<? super KnownDomainObject<TestSuiteComponent>>)action);
	}

	@Override
	public <T extends TestSuiteComponent> void whenElementKnown(Class<T> type, Action<? super KnownComponent<? extends T>> action) {
		doWhenElementKnown(type, (Action<? super KnownDomainObject<T>>)action);
	}

	@Override
	protected <U extends TestSuiteComponent> TypeAwareDomainObjectIdentifier<U> newIdentifier(String name, Class<U> type) {
		// FIXME: Throw exception if type is not implementing TestSuiteComponent
		return ComponentIdentifier.of(ComponentName.of(name), type, owner);
	}

	@Override
	public <U extends TestSuiteComponent> void registerBinding(Class<U> type, Class<? extends U> implementationType) {
		super.registerBinding(type, implementationType);
		bindings.put(type, implementationType);
	}

	@Override
	public <U extends TestSuiteComponent> DomainObjectProvider<U> register(String name, Class<U> type) {
		return super.register(name, (Class<U>)bindings.getOrDefault(type, type)); // FIXME: move binding redirect into newIdentifier
	}

	@Override
	public <U extends TestSuiteComponent> DomainObjectProvider<U> register(String name, Class<U> type, Action<? super U> action) {
		return super.register(name, (Class<U>)bindings.getOrDefault(type, type), action); // FIXME: move binding redirect into newIdentifier
	}
}
