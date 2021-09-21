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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.isDescendent;

public final class DomainObjectActions<T> implements Consumer<T> {
	private final List<Consumer<? super T>> configureActions = new ArrayList<>();

	@Override
	public void accept(T t) {
		configureActions.forEach(action -> action.accept(t));
	}

	public void add(Consumer<? super T> action) {
		configureActions.add(action);
	}

	public static <T, S extends T> Consumer<T> onlyIf(Class<S> type, Consumer<? super S> action) {
		return new Consumer<T>() {
			@Override
			public void accept(T object) {
				if (type.isInstance(object)) {
					action.accept(type.cast(object));
				}
			}
		};
	}

	public static <T> BiConsumer<? super DomainObjectIdentifier, ? super T> onlyIf(DomainObjectIdentifier owner, Action<? super T> action) {
		return new BiConsumer<DomainObjectIdentifier, T>() {
			@Override
			public void accept(DomainObjectIdentifier identifier, T object) {
				if (isDescendent(identifier, owner)) {
					action.execute(object);
				}
			}
		};
	}
}
