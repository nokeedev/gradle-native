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

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

abstract class SerializableObjectAdapter<T extends Serializable> implements Serializable {
	protected SerializableObjectAdapter() {}

	protected abstract T delegate();

	// The following equals and hashCode is not the most efficient implementation
	//   but is a good enough implementation... for now.
	// When-if these become a measurable bottleneck, we can revisit those implementation.
	@Override
	public final boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SerializableObjectAdapter)) {
			return false;
		}
		SerializableObjectAdapter<?> that = (SerializableObjectAdapter<?>) o;
		return Arrays.equals(SerializationUtils.serialize(delegate()), SerializationUtils.serialize(that.delegate()));
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(SerializationUtils.serialize(delegate()));
	}

	@Override
	public final String toString() {
		return delegate().toString();
	}
}
