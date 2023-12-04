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

public final class TypeFilteringAction<ElementType, FilteredType> implements Action<ElementType> {
	private final TypeOf<FilteredType> type;
	private final Action<? super FilteredType> action;

	public TypeFilteringAction(TypeOf<FilteredType> type, Action<? super FilteredType> action) {
		this.type = type;
		this.action = action;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void execute(ElementType t) {
		if (type.isAssignableFrom(t.getClass())) {
			action.execute((FilteredType) t);
		}
	}

	public static <ElementType, FilteredType> Action<ElementType> ofType(Class<FilteredType> type, Action<? super FilteredType> action) {
		return new TypeFilteringAction<>(TypeOf.typeOf(type), action);
	}

	public static <ElementType, FilteredType> Action<ElementType> ofType(TypeOf<FilteredType> type, Action<? super FilteredType> action) {
		return new TypeFilteringAction<>(type, action);
	}
}
