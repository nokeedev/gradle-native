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
package dev.nokee.util.internal;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.function.Predicate;

@EqualsAndHashCode
public final class AlwaysFalsePredicate implements Predicate<Object>, Serializable {
	private static final AlwaysFalsePredicate INSTANCE = new AlwaysFalsePredicate();

	@Override
	public boolean test(Object ignored) {
		return false; // always
	}

	@SuppressWarnings("unchecked")
	public <T> Predicate<T> withNarrowType() {
		return (Predicate<T>) this;
	}

	public static <T> Predicate<T> alwaysFalse() {
		return INSTANCE.withNarrowType();
	}
}
