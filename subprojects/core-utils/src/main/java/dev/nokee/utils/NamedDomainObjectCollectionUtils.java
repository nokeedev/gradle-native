/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.utils;

import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.*;
import org.gradle.api.internal.DefaultDomainObjectCollection;
import org.gradle.api.internal.DefaultNamedDomainObjectCollection;

import java.util.*;
import java.util.function.*;

public final class NamedDomainObjectCollectionUtils {
	private NamedDomainObjectCollectionUtils() {}

	//region createIfAbsent
	@SuppressWarnings("unchecked")
	public static <T> T createIfAbsent(NamedDomainObjectContainer<T> self, String name, Action<? super T> action) {
		// TODO: Should assert the type base type
		return (T) findElement(self, name).map(byName(self, action)).orElseGet(createUsing(self, name, action));
	}

	@SuppressWarnings("unchecked")
	public static <U extends T, T> T createIfAbsent(PolymorphicDomainObjectContainer<T> self, String name, Class<U> type, Action<? super U> action) {
		return (U) findElement(self, name).map(assertNoMismatch(self, type)).map(byName(self, action)).orElseGet(createUsing(self, name, type, action));
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

	@SuppressWarnings("unchecked")
	private static <U extends T, T> Function<NamedDomainObjectCollectionSchema.NamedDomainObjectSchema, U> byName(NamedDomainObjectCollection<T> self, Action<? super U> action) {
		return element -> self.withType((Class<U>) element.getPublicType().getConcreteClass()).getByName(element.getName(), action);
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
			return self.named(name, action);
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

	public static <U extends T, T> NamedDomainObjectProvider<U> registerIfAbsent(PolymorphicDomainObjectContainer<T> self, String name, Class<U> type, Action<? super U> action) {
		if (hasElementWithName(self, name)) {
			// TODO: Assert type is correct one, be careful with Task and DefaultTask for TaskContainer
			return self.named(name, type, action);
		}

		return self.register(name, type, action);
	}
	//endregion

	//region whenElementKnown
	/**
	 * Adds callback when new element is known to the named collection.
	 * This API uses the {@link DefaultNamedDomainObjectCollection#whenElementKnown(Action)} internal API.
	 * The Gradle API can call back twice, once when registering the element and another time when the element is created.
	 * This API deduplicate callback execution to ensure a single callback per element.
	 *
	 * @param self  the collection, must not be null
	 * @param action  the action to call for each known element, must not be null
	 * @param <T>  the base element type
	 */
	public static <T> void whenElementKnown(NamedDomainObjectCollection<T> self, Action<? super KnownElement<T>> action) {
		((DefaultNamedDomainObjectCollection<T>) self).whenElementKnown(new ToKnownElementAction<T>(action) {
			@Override
			protected KnownElement<T> create(DefaultNamedDomainObjectCollection.ElementInfo<T> elementInfo) {
				return KnownElement.of(self, elementInfo);
			}
		});
	}

	private static abstract class ToKnownElementAction<T> implements Action<DefaultNamedDomainObjectCollection.ElementInfo<T>> {
		private final Set<String> deduplicateElements = new HashSet<>();
		private final Action<? super KnownElement<T>> action;

		ToKnownElementAction(Action<? super KnownElement<T>> action) {
			this.action = action;
		}

		@Override
		public final void execute(DefaultNamedDomainObjectCollection.ElementInfo<T> elementInfo) {
			if (deduplicateElements.add(elementInfo.getName())) {
				action.execute(create(elementInfo));
			}
		}

		protected abstract KnownElement<T> create(DefaultNamedDomainObjectCollection.ElementInfo<T> elementInfo);
	}
	//endregion

	//region getType

	/**
	 * Returns the type of element in the domain object collection.
	 *
	 * @param self  the domain object collection, must not be null
	 * @param <E>  the type of element in the domain object collection
	 * @return a class representing the type of elements in the domain object collection, never null
	 */
	@SuppressWarnings("unchecked")
	public static <E> Class<E> getElementType(DomainObjectCollection<E> self) {
		return (Class<E>) ((DefaultDomainObjectCollection<E>) self).getType();
	}
	//endregion

	@EqualsAndHashCode
	public static final class KnownElement<T> {
		private final NamedDomainObjectCollection<T> collection;
		private final String name;
		private final Class<T> type;

		private KnownElement(NamedDomainObjectCollection<T> collection, String name, Class<T> type) {
			this.collection = collection;
			this.name = name;
			this.type = type;
		}

		@SuppressWarnings("unchecked")
		private static <T> KnownElement<T> of(NamedDomainObjectCollection<T> collection, DefaultNamedDomainObjectCollection.ElementInfo<T> elementInfo) {
			return new KnownElement<>(collection, elementInfo.getName(), (Class<T>)elementInfo.getType());
		}

		public String getName() {
			return name;
		}

		public Class<T> getType() {
			return type;
		}

		public NamedDomainObjectProvider<T> getAsProvider() {
			return collection.named(name, type);
		}

		@Override
		public String toString() {
			return "ofKnownElement(" + name + ", " + type + ")";
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
