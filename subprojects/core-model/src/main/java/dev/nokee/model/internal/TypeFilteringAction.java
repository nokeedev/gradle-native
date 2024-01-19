/*
 * Copyright 2023 the original author or authors.
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

package dev.nokee.model.internal;

import org.gradle.api.Action;
import org.gradle.api.reflect.TypeOf;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.BiConsumer;

public final class TypeFilteringAction<ElementType, FilteredType> implements Action<ElementType> {
	private final ActionTypeOf<FilteredType> type;
	private final BiConsumer<? super ElementType, ? super FilteredType> action;
	@Nullable private final Action<?> delegate;

	public TypeFilteringAction(ActionTypeOf<FilteredType> type, BiConsumer<? super ElementType, ? super FilteredType> action, @Nullable Action<?> nestedAction) {
		this.type = type;
		this.action = action;
		this.delegate = nestedAction;
	}

	// FIXME(discovery): Improve unpackable-ity for action inspection
	public Optional<Action<?>> getDelegate() {
		return Optional.ofNullable(delegate);
	}

	@Override
	public void execute(ElementType t) {
		if (type.isInstance(t)) {
			action.accept(t, type.cast(t));
		}
	}

	public interface ActionTypeOf<T> {
		boolean isInstance(Object obj);
		T cast(Object obj);
	}

	private static final class JavaClassTypeOf<T> implements ActionTypeOf<T> {
		private final Class<T> type;

		private JavaClassTypeOf(Class<T> type) {
			this.type = type;
		}

		@Override
		public boolean isInstance(Object obj) {
			return type.isInstance(obj);
		}

		@Override
		public T cast(Object obj) {
			return type.cast(obj);
		}
	}

	private static final class GradleTypeOf<T> implements ActionTypeOf<T> {
		private final TypeOf<T> type;

		private GradleTypeOf(TypeOf<T> type) {
			this.type = type;
		}

		@Override
		public boolean isInstance(Object obj) {
			return type.getConcreteClass().isAssignableFrom(obj.getClass());
		}

		@Override
		@SuppressWarnings("unchecked")
		public T cast(Object obj) {
			return (T) obj;
		}
	}

	public static <ElementType, FilteredType> Action<ElementType> ofType(Class<FilteredType> type, Action<? super FilteredType> action) {
		return new TypeFilteringAction<>(new JavaClassTypeOf<>(type), (__, t) -> action.execute(t), action);
	}

	public static <ElementType, FilteredType> Action<ElementType> ofType(TypeOf<FilteredType> type, Action<? super FilteredType> action) {
		return new TypeFilteringAction<>(new GradleTypeOf<>(type), (__, t) -> action.execute(t), action);
	}

	public static <ElementType, FilteredType> Action<ElementType> ofType(Class<FilteredType> type, BiConsumer<? super ElementType, ? super FilteredType> action) {
		return new TypeFilteringAction<>(new JavaClassTypeOf<>(type), action, null);
	}

	public static <ElementType, FilteredType> Action<ElementType> ofType(TypeOf<FilteredType> type, BiConsumer<? super ElementType, ? super FilteredType> action) {
		return new TypeFilteringAction<>(new GradleTypeOf<>(type), action, null);
	}

	public static <ElementType, FilteredType> Action<ElementType> ofType(ActionTypeOf<FilteredType> type, Action<? super FilteredType> action) {
		return new TypeFilteringAction<>(type, (__, t) -> action.execute(t), action);
	}
}
