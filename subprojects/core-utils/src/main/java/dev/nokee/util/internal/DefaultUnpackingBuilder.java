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
package dev.nokee.util.internal;

import dev.nokee.util.Executable;
import dev.nokee.util.Unpacker;
import dev.nokee.utils.DeferredUtils;
import dev.nokee.utils.PredicateUtils;
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import java.util.function.Predicate;

@EqualsAndHashCode
public final class DefaultUnpackingBuilder<T> implements DeferredUtils.UnpackingBuilder<T> {
	private final Unpacker unpacker;

	public DefaultUnpackingBuilder(Unpacker unpacker) {
		this.unpacker = unpacker;
	}

	@Override
	public Executable<T> until(Class<?> type) {
		return new UnpackerExecutableAdapter<>(new NestableUnpacker(new PredicateBasedUnpacker(unpacker, PredicateUtils.until(type))));
	}

	@Override
	public Executable<T> whileTrue(Predicate<Object> predicate) {
		return new UnpackerExecutableAdapter<>(new NestableUnpacker(new PredicateBasedUnpacker(unpacker, predicate)));
	}

	@Override
	public T execute(@Nullable Object obj) {
		@SuppressWarnings("unchecked")
		T result = (T) new NestableUnpacker(unpacker).unpack(obj);
		return result;
	}
}
