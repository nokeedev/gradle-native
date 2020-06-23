package dev.nokee.platform.base.internal;

import dev.nokee.internal.Cast;
import dev.nokee.platform.base.Variant;
import lombok.Value;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class ComponentCollection<T extends Component> {
	private final Map<String, ComponentCreationArguments> componentCreationArguments = new HashMap<>();
	private final NamedDomainObjectContainer<Component> collection = getObjects().domainObjectContainer(Component.class, this::create);

	@Inject
	protected abstract ObjectFactory getObjects();

	private Component create(String name) {
		ComponentCreationArguments<T> args = componentCreationArguments.remove(name);
		T result = getObjects().newInstance(args.type, args.names);
		return result;
	}

	public <S extends T> ComponentProvider<S> register(Class<S> type, NamingScheme names) {
		componentCreationArguments.put(names.getComponentName(), new ComponentCreationArguments<>(type, names));
		return Cast.uncheckedCast("of type erasure", getObjects().newInstance(ComponentProvider.class, collection.register(names.getComponentName())));
	}

	public <S extends T> void configureEach(Class<S> type, Action<? super S> action) {
		collection.withType(type).configureEach(action);
	}

	@Value
	private static class ComponentCreationArguments<T extends Component> {
		Class<T> type;
		NamingScheme names;
	}
}
