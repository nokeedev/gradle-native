/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.platform.nativebase.internal;

import dev.nokee.model.DomainObjectProvider;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import java.util.function.Supplier;

public final class BaseName implements Supplier<String> {
	private final DomainObjectProvider<String> value;

	public BaseName(DomainObjectProvider<String> value) {
		this.value = value;
	}

	public String get() {
		return value.get();
	}

	public <S> Provider<S> map(Transformer<? extends S, ? super String> mapper) {
		return value.map(mapper);
	}

	@Override
	public String toString() {
		return get();
	}
}
