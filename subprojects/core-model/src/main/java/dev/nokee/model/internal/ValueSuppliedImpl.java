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

import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

final class ValueSuppliedImpl<T> implements Value<T> {
	private final Provider<T> provider;
	private final Class<T> type;
	private final Supplier<T> supplier;
	private final List<Transformer<? extends T, ? super T>> inPlaceMappers = new ArrayList<>();
	private T value = null;

	public ValueSuppliedImpl(Class<T> type, Supplier<T> supplier) {
		this.type = type;
		this.supplier = supplier;
		this.provider = ProviderUtils.supplied(this::get);
	}

	@Override
	public T get() {
		if (value == null) {
			value = supplier.get();
			for (val inPlaceMapper : inPlaceMappers) {
				value = inPlaceMapper.transform(value);
			}
			inPlaceMappers.clear();
		}
		return value;
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public <S> Provider<S> map(Transformer<? extends S, ? super T> mapper) {
		return provider.map(mapper);
	}

	@Override
	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> mapper) {
		return provider.flatMap(mapper);
	}

	@Override
	public Value<T> mapInPlace(Transformer<? extends T, ? super T> mapper) {
		inPlaceMappers.add(mapper);
		return this;
	}

	@Override
	public Provider<T> toProvider() {
		return provider;
	}
}
