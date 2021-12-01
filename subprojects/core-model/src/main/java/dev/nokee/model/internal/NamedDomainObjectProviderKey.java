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
package dev.nokee.model.internal;

import lombok.EqualsAndHashCode;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.internal.provider.ProviderInternal;

@EqualsAndHashCode
final class NamedDomainObjectProviderKey {
	public static NamedDomainObjectProviderKey of(NamedDomainObjectProvider<?> delegate) {
		return new NamedDomainObjectProviderKey(delegate.getName(), ((ProviderInternal) delegate).getType());
	}

	private final String name;
	private final Class<?> type;

	private NamedDomainObjectProviderKey(String name, Class<?> type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return type;
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", name, type.getSimpleName());
	}
}
