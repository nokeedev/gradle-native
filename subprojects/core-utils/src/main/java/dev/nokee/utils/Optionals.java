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
package dev.nokee.utils;

import org.gradle.api.reflect.TypeOf;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class Optionals {
	public static <T> Stream<T> stream(Optional<T> self) {
		if (self.isPresent()) {
			return Stream.of(self.get());
		} else {
			return Stream.empty();
		}
	}

	public static <T> void ifPresentOrElse(Optional<T> self, Consumer<? super T> action, Runnable emptyAction) {
		if (self.isPresent()) {
			action.accept(self.get());
		} else {
			emptyAction.run();
		}
	}

    public static <T> Optional<T> or(Optional<T> self, Supplier<? extends Optional<? extends T>> supplier) {
		Objects.requireNonNull(self);
		Objects.requireNonNull(supplier);
		if (self.isPresent()) {
			return self;
		} else {
			@SuppressWarnings("unchecked")
			Optional<T> result = (Optional<T>) supplier.get();
			return Objects.requireNonNull(result);
		}
    }

	public static <T> Function<Object, T> safeAs(Class<T> type) {
		return obj -> {
			if (type.isInstance(obj)) {
				return type.cast(obj);
			} else {
				return null;
			}
		};
	}

	public static <T> Function<Object, T> safeAs(TypeOf<T> type) {
		return obj -> {
			if (type.getConcreteClass().isInstance(obj)) {
				return type.getConcreteClass().cast(obj);
			} else {
				return null;
			}
		};
	}
}
