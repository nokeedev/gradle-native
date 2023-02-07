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

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Unpack the target object and return the result.
 *
 * <p>A packed object is implementation specific and doesn't represent any specific types.
 * For example, a packer may target Java {@code Callable}, Gradle {@code Provider} or Kotlin {@code Function}.
 */
public interface Unpacker {
	@Nullable
	Object unpack(@Nullable Object target);

	default boolean canUnpack(@Nullable Object target) {
		return !Objects.equals(target, unpack(target)); // very inefficient implementation
	}
}
