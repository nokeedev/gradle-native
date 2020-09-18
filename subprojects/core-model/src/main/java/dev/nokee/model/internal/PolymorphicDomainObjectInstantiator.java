package dev.nokee.model.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.model.DomainObjectFactory;
import dev.nokee.model.DomainObjectFactoryRegistry;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectInstantiator;
import org.gradle.api.InvalidUserDataException;

import java.util.*;

public class PolymorphicDomainObjectInstantiator<T> implements DomainObjectInstantiator<T>, DomainObjectFactoryRegistry<T> {
	private final Map<Class<? extends T>, DomainObjectFactory<? extends T>> factories = new HashMap<>();
	private final Class<? extends T> baseType;
	private final String displayName;

	public PolymorphicDomainObjectInstantiator(Class<? extends T> type, String displayName) {
		this.baseType = type;
		this.displayName = displayName;
	}

	@Override
	public <U extends T> void registerFactory(Class<U> type, DomainObjectFactory<? extends U> factory) {
		if (!baseType.isAssignableFrom(type)) {
			String message = String.format("Cannot register a factory for type %s because it is not a subtype of container element type %s.", type.getSimpleName(), baseType.getSimpleName());
			throw new IllegalArgumentException(message);
		}
		if (factories.containsKey(type)) {
			throw new RuntimeException(String.format("Cannot register a factory for type %s because a factory for this type is already registered.", type.getSimpleName()));
		}
		factories.put(type, factory);
	}

	@Override
	public <S extends T> S newInstance(DomainObjectIdentifier identifier, Class<S> type) {
		@SuppressWarnings("unchecked")
        DomainObjectFactory<S> factory = (DomainObjectFactory<S>) factories.get(type);
		if (factory == null) {
			throw uncreatableTypeException(type);
		}
		return factory.create(identifier);
	}

	private RuntimeException uncreatableTypeException(Class<?> type) {
		return new InvalidUserDataException(
			String.format("Cannot create a %s because this type is not known to %s. Known types are: %s", type.getSimpleName(), displayName, getSupportedTypeNames()),
			new NoFactoryRegisteredForTypeException());
	}

	private String getSupportedTypeNames() {
		List<String> names = new ArrayList<>();
		for (Class<?> clazz : factories.keySet()) {
			names.add(clazz.getSimpleName());
		}
		Collections.sort(names);
		return names.isEmpty() ? "(None)" : String.join(", ", names);
	}

	public Set<? extends Class<? extends T>> getCreatableTypes() {
		return ImmutableSet.copyOf(factories.keySet());
	}

	public void assertCreatableType(Class<?> type) {
		if (!factories.containsKey(type)) {
			throw uncreatableTypeException(type);
		}
	}

	private static final class NoFactoryRegisteredForTypeException extends RuntimeException {
		public NoFactoryRegisteredForTypeException() {}
	}
}
