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
package dev.nokee.xcode.project.coders;

import com.google.common.collect.Iterators;
import dev.nokee.internal.testing.invocations.HasInvocationResults;
import dev.nokee.internal.testing.invocations.InvocationResult;
import dev.nokee.internal.testing.reflect.ArgumentInformation;
import dev.nokee.xcode.project.ValueDecoder;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class WrapDecoder<T> implements ValueDecoder<WrapDecoder.Wrapper<T>, T>, HasInvocationResults<ArgumentInformation.Arg2<T, ValueDecoder.Context>> {
	private final List<InvocationResult<ArgumentInformation.Arg2<T, ValueDecoder.Context>>> invocations = new ArrayList<>();
	private final CoderType<T> inputType;

	public WrapDecoder(CoderType<T> inputType) {
		this.inputType = inputType;
	}

	@Override
	public List<InvocationResult<ArgumentInformation.Arg2<T, ValueDecoder.Context>>> getAllInvocations() {
		return invocations;
	}

	@Override
	public Wrapper<T> decode(T object, Context context) {
		invocations.add(new InvocationResult<ArgumentInformation.Arg2<T, ValueDecoder.Context>>() {
			@Override
			public Iterator<Object> iterator() {
				return Iterators.forArray(object, context);
			}
		});
		return new Wrapper<>(object);
	}

	@Override
	public CoderType<?> getDecodeType() {
		return wrapper(inputType);
	}

	public static <T> CoderType<Wrapper<T>> wrapper(CoderType<T> type) {
		return new WrapperOfType<>(type);
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class WrapperOfType<T> extends CoderType<Wrapper<T>> {
		private final CoderType<T> wrappedType;

		private WrapperOfType(CoderType<T> wrappedType) {
			this.wrappedType = wrappedType;
		}

		@Override
		public String toString() {
			return "wrapper of " + wrappedType;
		}
	}

	public static <T> Wrapper<T> wrap(T instance) {
		return new Wrapper<>(instance);
	}

	@EqualsAndHashCode
	public static final class Wrapper<T> {
		private final T value;

		public Wrapper(T value) {
			this.value = value;
		}
	}
}
