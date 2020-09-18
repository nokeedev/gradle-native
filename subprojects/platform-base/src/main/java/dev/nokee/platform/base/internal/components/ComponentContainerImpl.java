package dev.nokee.platform.base.internal.components;

import dev.nokee.model.DomainObjectFactory;
import dev.nokee.model.internal.NokeeMap;
import dev.nokee.model.internal.NokeeMapImpl;
import dev.nokee.model.internal.PolymorphicDomainObjectInstantiator;
import dev.nokee.model.internal.Value;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.DomainObjectProvider;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.DomainObjectProviderValueAdapter;
import dev.nokee.platform.base.internal.ProjectIdentifier;
import dev.nokee.utils.TransformerUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;
import java.util.Set;

public class ComponentContainerImpl implements ComponentContainerInternal {
	private final PolymorphicDomainObjectInstantiator<Component> instantiator = new PolymorphicDomainObjectInstantiator<>(Component.class, "component");
	private final ProjectIdentifier projectIdentifier;
	private final NokeeMap<ComponentIdentifier<? extends Component>, Component> store;

	@Inject
	public ComponentContainerImpl(ProjectIdentifier projectIdentifier, ObjectFactory objectFactory) {
		this.projectIdentifier = projectIdentifier;
		this.store = new NokeeMapImpl<>(Component.class, objectFactory);
	}

	@Override
	public <T extends Component> DomainObjectProvider<T> register(String name, Class<T> type) {
		instantiator.assertCreatableType(type);

		val identifier = ComponentIdentifier.of(ComponentName.of(name), type, projectIdentifier);
		val value = Value.supplied(type, () -> instantiator.newInstance(identifier, type));

		@SuppressWarnings("unchecked")
		val componentValue = (Value<Component>) value;
		store.put(identifier, componentValue);

		return new DomainObjectProviderValueAdapter<>(identifier, value);
	}

	@Override
	public <T extends Component> DomainObjectProvider<T> register(String name, Class<T> type, Action<? super T> action) {
		instantiator.assertCreatableType(type);

		val identifier = ComponentIdentifier.of(ComponentName.of(name), type, projectIdentifier);
		val value = Value.supplied(type, () -> instantiator.newInstance(identifier, type));

		@SuppressWarnings("unchecked")
		val componentValue = (Value<Component>) value;
		store.put(identifier, componentValue);

		value.mapInPlace(TransformerUtils.configureInPlace(action));
		return new DomainObjectProviderValueAdapter<>(identifier, value);
	}

	@Override
	public <U extends Component> void registerFactory(Class<U> type, DomainObjectFactory<? extends U> factory) {
		instantiator.registerFactory(type, factory);
	}

	@Override
	public ComponentContainerInternal disallowChanges() {
		store.disallowChanges();
		return this;
	}

	@Override
	public Provider<Set<? extends Component>> getElements() {
		return store.values().getElements().map(TransformerUtils.toSetTransformer());
	}
}
