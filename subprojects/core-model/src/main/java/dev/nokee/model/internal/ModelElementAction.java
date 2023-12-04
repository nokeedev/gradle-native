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

import java.util.function.BiConsumer;

public final class ModelElementAction<ElementType> implements Action<ElementType> {
	private final BiConsumer<? super ModelElement, ? super ElementType> action;

	public ModelElementAction(BiConsumer<? super ModelElement, ? super ElementType> action) {
		this.action = action;
	}

	@Override
	public void execute(ElementType t) {
		ModelElementSupport.safeAsModelElement(t).ifPresent(element -> {
			action.accept(element, t);
		});
	}

	public static <ElementType> ModelElementAction<ElementType> withElement(BiConsumer<? super ModelElement, ? super ElementType> action) {
		return new ModelElementAction<>(action);
	}

	public static <ElementType, T> BiConsumer<ElementType, T> withElement(BiModelElementConsumer<? super ElementType, ? super T> action) {
		return (t, u) -> {
			ModelElementSupport.safeAsModelElement(t).ifPresent(element -> {
				action.accept(element, t, u);
			});
		};
	}

	public interface BiModelElementConsumer<A, B> {
		void accept(ModelElement e, A a, B b);
	}
}
