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
package dev.nokee.util;

import org.gradle.api.Transformer;
import org.gradle.api.provider.HasMultipleValues;
import org.gradle.api.provider.Provider;

public final class ProviderOfIterableTransformer<ElementType> implements Transformer<Provider<? extends Iterable<? extends ElementType>>, Iterable<? extends Provider<? extends ElementType>>> {
	private final CollectionContainerFactory<?> containerFactory;

	private ProviderOfIterableTransformer(CollectionContainerFactory<?> containerFactory) {
		this.containerFactory = containerFactory;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Provider<? extends Iterable<? extends ElementType>> transform(Iterable<? extends Provider<? extends ElementType>> providers) {
		final Provider<?> container = containerFactory.create(Object.class);
		for (Provider<? extends ElementType> provider : providers) {
			((HasMultipleValues<Object>) container).add(provider);
		}

		@SuppressWarnings("unchecked")
		Provider<? extends Iterable<ElementType>> result = (Provider<? extends Iterable<ElementType>>) container;
		return result;
	}

	public static <ElementType> Transformer<Provider<? extends Iterable<? extends ElementType>>, Iterable<? extends Provider<? extends ElementType>>> toProviderOfIterable(CollectionContainerFactory<?> containerFactory) {
		return new ProviderOfIterableTransformer<>(containerFactory);
	}

	public interface CollectionContainerFactory<ContainerType extends Provider<? extends Iterable<?>>> {
		ContainerType create(Class<?> elementType);
	}
}
