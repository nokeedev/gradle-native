/*
 * Copyright 2024 the original author or authors.
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
import org.gradle.api.specs.Spec;

public final class IdentifierFilteringAction<ElementType> implements Action<ElementType> {
	private final IdentifierOf<ElementType> spec;
	private final Action<? super ElementType> delegate;

	public IdentifierFilteringAction(IdentifierOf<ElementType> spec, Action<? super ElementType> delegate) {
		this.spec = spec;
		this.delegate = delegate;
	}

	interface IdentifierOf<T> extends Spec<T> {}

	static final class ModelElementIdentifierOf<T> implements IdentifierOf<T> {
		private final Spec<? super ModelObjectIdentifier> spec;

		ModelElementIdentifierOf(Spec<? super ModelObjectIdentifier> spec) {
			this.spec = spec;
		}

		@Override
		public boolean isSatisfiedBy(T t) {
			return ModelElementSupport.safeAsModelElement(t).map(ModelElement::getIdentifier).map(spec::isSatisfiedBy).orElse(false);
		}
	}

	@Override
	public void execute(ElementType elementType) {
		if (spec.isSatisfiedBy(elementType)) {
			delegate.execute(elementType);
		}
	}

	public static <U> IdentifierFilteringAction<U> descendantOf(ModelObjectIdentifier baseIdentifier, Action<? super U> action) {
		return new IdentifierFilteringAction<>(new ModelElementIdentifierOf<>(descendantOf(baseIdentifier)), action);
	}

	public static Spec<ModelObjectIdentifier> descendantOf(ModelObjectIdentifier baseIdentifier) {
		return it -> ModelObjectIdentifiers.descendantOf(it, baseIdentifier);
	}
}
