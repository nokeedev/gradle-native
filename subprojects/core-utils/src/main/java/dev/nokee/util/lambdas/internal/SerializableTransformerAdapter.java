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
package dev.nokee.util.lambdas.internal;

import dev.nokee.util.lambdas.SerializableTransformer;
import org.gradle.api.Transformer;

import java.io.Serializable;
import java.util.Objects;

public final class SerializableTransformerAdapter<OUT, IN> extends SerializableObjectAdapter<SerializableTransformer<OUT, IN>> implements Transformer<OUT, IN>, Serializable {
	private final SerializableTransformer<OUT, IN> delegate;

	public SerializableTransformerAdapter(SerializableTransformer<OUT, IN> delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	public OUT transform(IN in) {
		return delegate.transform(in);
	}

	@Override
	protected SerializableTransformer<OUT, IN> delegate() {
		return delegate;
	}
}
