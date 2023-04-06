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
package dev.nokee.util.provider;

import dev.nokee.utils.ProviderUtils;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

/**
 * Convenient builder when no value has being specified.
 */
public final class ZipProviderBuilder0 implements ZipProviderBuilder {
	private final ObjectFactory objects;

	// Use ZipProviderBuilder#newBuilder(objects)
	ZipProviderBuilder0(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public <T> ZipProviderBuilder1<T> value(Provider<? extends T> provider) {
		return new ZipProviderBuilder1<>(objects, provider);
	}

	@Override
	public <R> Provider<R> zip(Combiner<R> combiner) {
		return ProviderUtils.notDefined(); // Nothing is being zipped
	}
}
