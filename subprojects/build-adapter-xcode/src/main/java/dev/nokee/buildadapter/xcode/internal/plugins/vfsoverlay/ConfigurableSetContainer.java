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

import com.google.common.reflect.TypeToken;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;

import javax.inject.Inject;
import java.util.Set;

// Note: we cannot declare this class as Iterable<T> because we are typically using it in a @Nested context
//   In a @Nested context, Gradle will prefer unpacking (iterate) the object instead of looking for more
//   @Nested properties which would discover the implicit task dependencies. Instead, users should use
//   #getElements() provider and iterate the value.
public abstract class ConfigurableSetContainer<T> {
	public T create(Action<? super T> action) {
		final T result = getObjects().newInstance(defaultType());
		action.execute(result);
		getValues().add(result);
		return result;
	}

	public <S extends T> S create(Class<S> type, Action<? super S> action) {
		final S result = getObjects().newInstance(type);
		action.execute(result);
		getValues().add(result);
		return result;
	}

	@SuppressWarnings("unchecked")
	private Class<T> defaultType() {
		return (Class<T>) new TypeToken<T>(getClass()) {}.getRawType();
	}

	public boolean addAll(Iterable<? extends T> l) {
		getValues().addAll(l);
		return true;
	}

	public boolean addAll(Provider<? extends Iterable<? extends T>> provider) {
		getValues().addAll(provider);
		return true;
	}

	public boolean addAll(ConfigurableSetContainer<? extends T> container) {
		getValues().addAll(container.getValues());
		return true;
	}

	public void clear() {
		getValues().empty();
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Internal
	protected abstract SetProperty<T> getValues();

	@Nested
	public Provider<Set<T>> getElements() {
		return getValues();
	}

	public ConfigurableSetContainer<T> configure(Action<? super ConfigurableSetContainer<T>> action) {
		action.execute(this);
		return this;
	}
}
