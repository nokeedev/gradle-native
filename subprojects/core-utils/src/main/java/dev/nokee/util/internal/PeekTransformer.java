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

import dev.nokee.utils.TransformerUtils;
import lombok.EqualsAndHashCode;
import org.gradle.api.Transformer;

import java.util.Objects;
import java.util.function.Consumer;

@EqualsAndHashCode
public final class PeekTransformer<T> implements Transformer<T, T> {
	private final Consumer<? super T> action;

	public PeekTransformer(Consumer<? super T> action) {
		this.action = Objects.requireNonNull(action);
	}

	@Override
	public T transform(T in) {
		action.accept(in);
		return in;
	}

	@Override
	public String toString() {
		return "TransformerUtils.peek(" + action + ")";
	}
}
