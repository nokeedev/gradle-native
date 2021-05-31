package dev.nokee.utils;

import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.*;
import org.gradle.api.internal.DefaultNamedDomainObjectCollection;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class NamedDomainObjectCollectionUtils {
	private NamedDomainObjectCollectionUtils() {}

	//region createIfAbsent
	public static <T> T createIfAbsent(NamedDomainObjectContainer<T> self, String name, Action<? super T> action) {
		// TODO: Wire in assertable
		// TODO: Should assert the type base type
		return (T) findElement(self, name).map(byName(self)).orElseGet(createUsing(self, name, action));
	}

	public static <U extends T, T> T createIfAbsent(PolymorphicDomainObjectContainer<T> self, String name, Class<U> type, Action<? super U> action) {
		// TODO: Wire in assertable
		return (U) findElement(self, name).map(assertNoMismatch(self, type)).map(byName(self)).orElseGet(createUsing(self, name, type, action));
	}
	//endregion

	private static <T> Supplier<T> createUsing(NamedDomainObjectContainer<T> self, String name, Action<? super T> action) {
		return () -> self.create(name, action);
	}

	private static <U extends T, T> Supplier<U> createUsing(PolymorphicDomainObjectContainer<T> self, String name, Class<U> type, Action<? super U> action) {
		return () -> self.create(name, type, action);
	}

	private static <U> UnaryOperator<NamedDomainObjectCollectionSchema.NamedDomainObjectSchema> assertNoMismatch(PolymorphicDomainObjectContainer<? super U> self, Class<U> expectedType) {
		return element -> {
			val actualType = element.getPublicType().getConcreteClass();
			if (!expectedType.equals(actualType)) {
				throw new InvalidUserDataException(String.format("Could not register element '%s': Element type requested (%s) does not match actual type (%s).", element.getName(), expectedType.getCanonicalName(), actualType.getCanonicalName()));
			}
			return element;
		};
	}

	private static <U extends T, T> Function<NamedDomainObjectCollectionSchema.NamedDomainObjectSchema, U> byName(NamedDomainObjectCollection<T> self) {
		return element -> (U) self.getByName(element.getName());
	}

	//region registerIfAbsent
	public static <T> NamedDomainObjectProvider<T> registerIfAbsent(NamedDomainObjectContainer<T> self, String name) {
		if (hasElementWithName(self, name)) {
			return self.named(name);
		}

		return self.register(name);
	}

	public static <T> NamedDomainObjectProvider<T> registerIfAbsent(NamedDomainObjectContainer<T> self, String name, Action<? super T> action) {
		if (hasElementWithName(self, name)) {
			// TODO: Wire in assertable
			return self.named(name);
		}

		return self.register(name, action);
	}

	public static <U extends T, T> NamedDomainObjectProvider<U> registerIfAbsent(PolymorphicDomainObjectContainer<T> self, String name, Class<U> type) {
		if (hasElementWithName(self, name)) {
			// TODO: Assert type is correct one, be careful with Task and DefaultTask for TaskContainer
			return self.named(name, type);
		}

		return self.register(name, type);
	}

	// TODO: The action here should force to be assertable to make the contract clear that when you registerIfAbsent, the action should match the base configuration of the existing element
	public static <U extends T, T> NamedDomainObjectProvider<U> registerIfAbsent(PolymorphicDomainObjectContainer<T> self, String name, Class<U> type, Action<? super U> action) {
		if (hasElementWithName(self, name)) {
			// TODO: Wire in assertable
			// TODO: Assert type is correct one, be careful with Task and DefaultTask for TaskContainer
			return self.named(name, type);
		}

		return self.register(name, type, action);
	}
	//endregion

	//region whenElementKnown
	public static <T> void whenElementKnown(NamedDomainObjectCollection<T> self, Action<? super ElementInfo<T>> action) {
		((DefaultNamedDomainObjectCollection<T>) self).whenElementKnown(compose(action, ElementInfo::of));
	}
	//endregion

	@EqualsAndHashCode
	public static final class ElementInfo<T> {
		private final String name;
		private final Class<T> type;

		private ElementInfo(String name, Class<T> type) {
			this.name = name;
			this.type = type;
		}

		@SuppressWarnings("unchecked")
		private static <T> ElementInfo<T> of(DefaultNamedDomainObjectCollection.ElementInfo<T> elementInfo) {
			return new ElementInfo<>(elementInfo.getName(), (Class<T>)elementInfo.getType());
		}

		public String getName() {
			return name;
		}

		public Class<T> getType() {
			return type;
		}

		@Override
		public String toString() {
			return "ofElementInfo(" + name + ", " + type + ")";
		}
	}

	private static <T> boolean hasElementWithName(NamedDomainObjectCollection<T> self, String name) {
		for (val element : self.getCollectionSchema().getElements()) {
			if (element.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	private static <T> Optional<NamedDomainObjectCollectionSchema.NamedDomainObjectSchema> findElement(NamedDomainObjectCollection<T> self, String name) {
		for (val element : self.getCollectionSchema().getElements()) {
			if (element.getName().equals(name)) {
				return Optional.of(element);
			}
		}
		return Optional.empty();
	}
}
