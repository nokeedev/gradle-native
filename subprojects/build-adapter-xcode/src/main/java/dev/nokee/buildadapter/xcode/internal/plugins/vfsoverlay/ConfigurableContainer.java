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
package dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import com.google.common.reflect.TypeToken;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;

import javax.inject.Inject;
import java.util.Map;
import java.util.function.Function;

// Note: we cannot declare this class as Iterable<T> because we are typically using it in a @Nested context
//   In a @Nested context, Gradle will prefer unpacking (iterate) the object instead of looking for more
//   @Nested properties which would discover the implicit task dependencies. Instead, users should use
//   #getElements() provider and iterate the value.
public abstract class ConfigurableContainer<T extends NamedDomainObject> {
	public T create(String name, Action<? super T> action) {
		T result = getObjects().newInstance(defaultType());
		result.getName().set(name);
		result.getName().finalizeValue();
		action.execute(result);
		getValues().put(name, result);
		return result;
	}

	@SuppressWarnings("unchecked")
	private Class<T> defaultType() {
		return (Class<T>) new TypeToken<T>(getClass()) {
		}.getRawType();
	}

	public boolean addAll(Iterable<? extends T> l) {
		l.forEach(it -> getValues().put(it.getName().get(), it));
		return true;
	}

	public boolean addAll(Provider<? extends Iterable<? extends T>> provider) {
		getValues().putAll(provider.map(it -> Streams.stream(it).collect(ImmutableMap.toImmutableMap(t -> t.getName().get(), Function.identity()))));
		return true;
	}

	public boolean addAll(ConfigurableContainer<? extends T> container) {
		getValues().putAll(container.getValues());
		return true;
	}

	public void clear() {
		getValues().empty();
	}

	public void named(String name, Action<? super T> action) {
		action.execute(getValues().getting(name).get());
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Internal
	protected abstract MapProperty<String, T> getValues();

	@Nested
	public Provider<Iterable<T>> getElements() {
		return getValues().map(Map::values);
	}

	public ConfigurableContainer<T> configure(Action<? super ConfigurableContainer<T>> action) {
		action.execute(this);
		return this;
	}
}
