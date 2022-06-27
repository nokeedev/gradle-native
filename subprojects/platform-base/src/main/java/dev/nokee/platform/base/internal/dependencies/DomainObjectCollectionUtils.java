/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.platform.base.internal.dependencies;

import lombok.val;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.provider.Provider;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

final class DomainObjectCollectionUtils {
	public static <SELF, E> BiConsumer<SELF, DomainObjectCollection<E>> add(Object element) {
		return (self, collection) -> addElementOrProviderOfElement(collection, element);
	}

	public static <SELF, E> BiConsumer<SELF, DomainObjectCollection<E>> add(Supplier<? extends Object> elementSupplier) {
		return (self, collection) -> addElementOrProviderOfElement(collection, elementSupplier.get());
	}

	private static <E> void addElementOrProviderOfElement(DomainObjectCollection<E> self, Object element) {
		if (element instanceof Provider) {
			@SuppressWarnings("unchecked")
			val elementProvider = (Provider<E>) element;
			self.addLater(elementProvider);
		} else {
			@SuppressWarnings("unchecked")
			val castedElement = (E) element;
			self.add(castedElement);
		}
	}
}
