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
import dev.nokee.internal.testing.invocations.InvocationResult2;
import dev.nokee.xcode.project.ValueEncoder;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class UnwrapEncoder<T> implements ValueEncoder<T, UnwrapEncoder.Wrapper<T>>, HasInvocationResults<InvocationResult2<UnwrapEncoder.Wrapper<T>, ValueEncoder.Context>> {
	private final List<InvocationResult2<UnwrapEncoder.Wrapper<T>, Context>> invocations = new ArrayList<>();
	private final CoderType<T> outputType;

	public UnwrapEncoder(CoderType<T> outputType) {
		this.outputType = outputType;
	}

	@Override
	public List<InvocationResult2<UnwrapEncoder.Wrapper<T>, Context>> getAllInvocations() {
		return invocations;
	}

	@Override
	public T encode(Wrapper<T> object, Context context) {
		invocations.add(new InvocationResult2<Wrapper<T>, Context>() {
			@Override
			public Iterator<Object> iterator() {
				return Iterators.forArray(object, context);
			}
		});
		return object.value;
	}

	@Override
	public CoderType<?> getEncodeType() {
		return wrapper(outputType);
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
