package dev.nokee.platform.base.internal.components;

import dev.nokee.model.internal.*;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.model.internal.ProjectIdentifier;
import org.gradle.api.Action;

public final class ComponentContainerImpl extends AbstractDomainObjectContainer<Component, Component> implements ComponentContainerInternal {
	private final ProjectIdentifier owner;

	public ComponentContainerImpl(ProjectIdentifier owner, ComponentConfigurer configurer, DomainObjectEventPublisher eventPublisher, ComponentProviderFactory providerFactory, ComponentRepository repository, KnownComponentFactory knownComponentFactory, ComponentInstantiator instantiator) {
		super(owner, Component.class, instantiator, configurer, eventPublisher, providerFactory, repository, knownComponentFactory);
		this.owner = owner;
	}

	@Override
	public ComponentContainerImpl disallowChanges() {
		super.disallowChanges();
		return this;
	}

	@Override
	protected <U extends Component> TypeAwareDomainObjectIdentifier<U> newIdentifier(String name, Class<U> type) {
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
}
