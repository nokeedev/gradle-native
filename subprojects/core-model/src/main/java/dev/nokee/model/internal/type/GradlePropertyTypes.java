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
package dev.nokee.model.internal.type;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.internal.provider.AbstractCollectionProperty;
import org.gradle.api.internal.provider.DefaultMapProperty;
import org.gradle.api.internal.provider.Providers;
import org.gradle.api.provider.*;

public final class GradlePropertyTypes {
	@SuppressWarnings({"unchecked", "UnstableApiUsage"})
	public static <T> ModelType<Property<T>> property(ModelType<T> type) {
		return (ModelType<Property<T>>) ModelType.of(new TypeToken<Property<T>>() {}
			.where(new TypeParameter<T>() {}, (TypeToken<T>) TypeToken.of(type.getType())).getType());
	}

	@SuppressWarnings({"unchecked", "UnstableApiUsage"})
	public static <T> ModelType<SetProperty<T>> setProperty(ModelType<T> type) {
		return (ModelType<SetProperty<T>>) ModelType.of(new TypeToken<SetProperty<T>>() {}
			.where(new TypeParameter<T>() {}, (TypeToken<T>) TypeToken.of(type.getType())).getType());
	}

	@SuppressWarnings({"unchecked", "UnstableApiUsage"})
	public static <T> ModelType<ListProperty<T>> listProperty(ModelType<T> type) {
		return (ModelType<ListProperty<T>>) ModelType.of(new TypeToken<ListProperty<T>>() {}
			.where(new TypeParameter<T>() {}, (TypeToken<T>) TypeToken.of(type.getType())).getType());
	}

	@SuppressWarnings({"unchecked", "UnstableApiUsage"})
	public static <K, V> ModelType<MapProperty<K, V>> mapProperty(ModelType<K> keyType, ModelType<V> valueType) {
		return (ModelType<MapProperty<K, V>>) ModelType.of(new TypeToken<MapProperty<K, V>>() {}
			.where(new TypeParameter<K>() {}, (TypeToken<K>) TypeToken.of(keyType.getType()))
			.where(new TypeParameter<V>() {}, (TypeToken<V>) TypeToken.of(valueType.getType()))
			.getType());
	}

	@SuppressWarnings({"unchecked", "UnstableApiUsage", "rawtypes"})
	public static <T extends HasConfigurableValue> ModelType<T> of(T instance) {
		if (instance instanceof Property) {
			return property(ModelType.of(Providers.internal((Property) instance).getType()));
		} else if (instance instanceof SetProperty) {
			return setProperty(ModelType.of(((AbstractCollectionProperty) instance).getElementType()));
		} else if (instance instanceof ListProperty) {
			return listProperty(ModelType.of(((AbstractCollectionProperty) instance).getElementType()));
		} else if (instance instanceof MapProperty) {
			return mapProperty(ModelType.of(((DefaultMapProperty) instance).getKeyType()), ModelType.of(((DefaultMapProperty) instance).getValueType()));
		} else if (instance instanceof ConfigurableFileCollection) {
			return (ModelType<T>) ModelType.of(ConfigurableFileCollection.class);
		} else {
			throw new UnsupportedOperationException();
		}
	}
}
