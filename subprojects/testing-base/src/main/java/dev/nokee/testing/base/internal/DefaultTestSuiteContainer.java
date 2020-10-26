package dev.nokee.testing.base.internal;

import dev.nokee.model.internal.*;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.components.*;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.testing.base.TestSuiteContainer;
import org.gradle.api.Action;

public final class DefaultTestSuiteContainer extends AbstractDomainObjectContainer<Component, TestSuiteComponent> implements TestSuiteContainer {
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
		return ComponentIdentifier.of(ComponentName.of(name), type, owner);
	}
}
