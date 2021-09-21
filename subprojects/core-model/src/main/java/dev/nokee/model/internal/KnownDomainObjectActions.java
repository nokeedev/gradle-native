/*
 * Copyright 2020 the original author or authors.
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

import dev.nokee.model.DomainObjectIdentifier;
import org.gradle.api.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.isDescendent;

public final class KnownDomainObjectActions<T> implements Consumer<TypeAwareDomainObjectIdentifier<? extends T>> {
	private final List<Consumer<? super TypeAwareDomainObjectIdentifier<? extends T>>> knownConfigureActions = new ArrayList<>();

	@Override
	public void accept(TypeAwareDomainObjectIdentifier<? extends T> knownDomainObjectIdentifier) {
		knownConfigureActions.forEach(action -> action.accept(knownDomainObjectIdentifier));
	}

	public void add(Consumer<? super TypeAwareDomainObjectIdentifier<? extends T>> action) {
		knownConfigureActions.add(action);
	}

	public static <T> Consumer<? super TypeAwareDomainObjectIdentifier<?>> onlyIf(DomainObjectIdentifier owner, Class<T> type, Action<? super TypeAwareDomainObjectIdentifier<T>> action) {
		return new Consumer<TypeAwareDomainObjectIdentifier<?>>() {
			@Override
			public void accept(TypeAwareDomainObjectIdentifier<?> knownDomainObjectIdentifier) {
				if (isDescendent(knownDomainObjectIdentifier, owner) && type.isAssignableFrom(knownDomainObjectIdentifier.getType())) {
					action.execute((TypeAwareDomainObjectIdentifier<T>)knownDomainObjectIdentifier);
				}
			}
		};
	}
}
