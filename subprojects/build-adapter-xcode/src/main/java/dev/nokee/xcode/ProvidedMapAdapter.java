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
package dev.nokee.xcode;

import com.google.common.collect.ImmutableMap;
import org.gradle.api.provider.Provider;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * An adapter for Gradle Provider to Map so we can avoid leaking Gradle APIs everywhere.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public final class ProvidedMapAdapter<K, V> implements Map<K, V> {
	private final Provider<Map<K, V>> delegate;

	public ProvidedMapAdapter(Provider<Map<K, V>> delegate) {
		assert delegate != null : "'delegate' must not be null";
		this.delegate = delegate;
	}

	@Override
	public int size() {
		return delegate().size();
	}

	@Override
	public boolean isEmpty() {
		return delegate().isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return delegate().containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return delegate().containsValue(value);
	}

	@Override
	public V get(Object key) {
		return delegate().get(key);
	}

	@Override
	public V put(K key, V value) {
		throw new UnsupportedOperationException("Map#put(K, V) is unsupported");
	}

	@Override
	public V remove(Object key) {
		throw new UnsupportedOperationException("Map#remote(Object) is unsupported");
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		throw new UnsupportedOperationException("Map#putAll(Map) is unsupported");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Map#clear() is unsupported");
	}

	@Override
	public Set<K> keySet() {
		return delegate().keySet();
	}

	@Override
	public Collection<V> values() {
		return delegate().values();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return delegate().entrySet();
	}

	private Map<K, V> delegate() {
		Map<K, V> result = delegate.getOrNull();
		if (result == null) {
			result = ImmutableMap.of(); // use empty map when provider is absent
		}
		return result;
	}
}
