package dev.nokee.model.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.model.DomainObjectFactory;
import dev.nokee.model.DomainObjectIdentifier;
import lombok.val;
import org.gradle.api.InvalidUserDataException;

import java.util.*;

public abstract class AbstractPolymorphicDomainObjectInstantiator<T> implements PolymorphicDomainObjectInstantiator<T> {
	private final Map<Class<? extends T>, DomainObjectFactory<? extends T>> factories = new HashMap<>();
	private final Class<? extends T> baseType;
	private final String displayName;

	protected AbstractPolymorphicDomainObjectInstantiator(Class<? extends T> type, String displayName) {
		this.baseType = type;
		this.displayName = displayName;
	}

	@Override
	public <U extends T> void registerFactory(Class<U> type, DomainObjectFactory<? extends U> factory) {
		val assertRegisteringType = registering(type);
		assertRegisteringType.isSubTypeOfBaseType();
		assertRegisteringType.isNotAlreadyRegistered();
		factories.put(type, factory);
	}

	@Override
	public <U extends T> void registerBinding(Class<U> type, Class<? extends U> implementationType) {
		val assertBindingType = binding(type);
		assertBindingType.isSubTypeOfBaseType();
		assertBindingType.isNotBindingToItself(implementationType);
		assertBindingType.isSuperTypeOf(implementationType);
		assertBindingType.isNotAlreadyRegistered();
		assertBindingType.isBindingToKnownType(implementationType);
		factories.put(type, factories.get(implementationType));
	}

	@Override
	public <U extends T> void registerFactoryIfAbsent(Class<U> type, DomainObjectFactory<? extends U> factory) {
		registering(type).isSubTypeOfBaseType();
		if (!factories.containsKey(type)) {
			factories.put(type, factory);
		}
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

	private BindingChecks binding(Class<?> bindingType) {
		return new BindingChecks(bindingType);
	}

	private RegisteringChecks registering(Class<?> registeringType) {
		return new RegisteringChecks(registeringType);
	}

	private final class RegisteringChecks {
		private final Class<?> registeringType;

		RegisteringChecks(Class<?> registeringType) {
			this.registeringType = registeringType;
		}

		public void isSubTypeOfBaseType() {
			if (!baseType.isAssignableFrom(registeringType)) {
				String message = String.format("Cannot register a factory for type %s because it is not a subtype of type %s.", registeringType.getSimpleName(), baseType.getSimpleName());
				throw new IllegalArgumentException(message);
			}
		}

		public void isNotAlreadyRegistered() {
			if (factories.containsKey(registeringType)) {
				throw new RuntimeException(String.format("Cannot register a factory for type %s because a factory for this type is already registered.", registeringType.getSimpleName()));
			}
		}
	}

	private final class BindingChecks {
		private final Class<?> bindingType;

		BindingChecks(Class<?> bindingType) {
			this.bindingType = bindingType;
		}

		void isNotBindingToItself(Class<?> implementationType) {
			if (bindingType.equals(implementationType)) {
				throw new IllegalArgumentException(String.format("Cannot bind type %s to itself.", bindingType.getSimpleName()));
			}
		}

		void isSuperTypeOf(Class<?> implementationType) {
			if (!bindingType.isAssignableFrom(implementationType)) {
				throw new IllegalArgumentException(String.format("Cannot bind type %s because it is not a supertype of type %s.", bindingType.getSimpleName(), implementationType.getSimpleName()));
			}
		}

		void isNotAlreadyRegistered() {
			if (factories.containsKey(bindingType)) {
				throw new RuntimeException(String.format("Cannot bind type %s because a factory for this type is already registered.", bindingType.getSimpleName()));
			}
		}

		void isBindingToKnownType(Class<?> implementationType) {
			if (!factories.containsKey(implementationType)) {
				throw new RuntimeException(String.format("Cannot bind type %s because a factory for type %s is not known to %s. Known types are: %s", bindingType.getSimpleName(), implementationType.getSimpleName(), displayName, getSupportedTypeNames()));
			}
		}

		public void isSubTypeOfBaseType() {
			if (!baseType.isAssignableFrom(bindingType)) {
				String message = String.format("Cannot bind type %s because it is not a subtype of type %s.", bindingType.getSimpleName(), baseType.getSimpleName());
				throw new IllegalArgumentException(message);
			}
		}
	}
}
