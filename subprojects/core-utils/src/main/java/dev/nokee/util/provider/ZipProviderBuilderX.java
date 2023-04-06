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

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;

import java.util.ArrayList;
import java.util.List;

// Use ZipProviderBuilder directly instead
//   This type is just a stand-in for zipping any number of elements.
final class ZipProviderBuilderX implements ZipProviderBuilder {
	private final ObjectFactory objects;
	private final List<Provider<?>> values = new ArrayList<>();

	// Use ZipProviderBuilder#newBuilder(objects)
	ZipProviderBuilderX(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public <U> ZipProviderBuilderX value(Provider<? extends U> provider) {
		values.add(provider);
		return this;
	}

	@Override
	public <R> Provider<R> zip(Combiner<R> combiner) {
		// We isolate the values so adding more values to this builder doesn't affect previous zip on that same builder
		final ListProperty<Object> accumulator = objects.listProperty(Object.class);
		values.forEach(accumulator::add);
		return accumulator.map(DefaultValuesToZip::new).map(combiner::combine);
	}
}
