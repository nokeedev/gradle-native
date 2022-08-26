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
package dev.nokee.gradle;

import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.internal.provider.Providers;
import org.gradle.util.GradleVersion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class NamedDomainObjectProviderFactory {
	private final ProviderFactory factory;

	public NamedDomainObjectProviderFactory() {
		if (GradleVersion.current().compareTo(GradleVersion.version("7.0")) >= 0) {
			this.factory = new ProviderFactory("dev.nokee.gradle.internal.v70.DefaultNamedDomainObjectProvider");
		} else if (GradleVersion.current().compareTo(GradleVersion.version("6.6")) >= 0) {
			this.factory = new ProviderFactory("dev.nokee.gradle.internal.v66.DefaultNamedDomainObjectProvider");
		} else if (GradleVersion.current().compareTo(GradleVersion.version("6.5")) >= 0) {
			this.factory = new ProviderFactory("dev.nokee.gradle.internal.v65.DefaultNamedDomainObjectProvider");
		} else if (GradleVersion.current().compareTo(GradleVersion.version("6.4")) >= 0) {
			this.factory = new ProviderFactory("dev.nokee.gradle.internal.v64.DefaultNamedDomainObjectProvider");
		} else {
			this.factory = new ProviderFactory("dev.nokee.gradle.internal.v62.DefaultNamedDomainObjectProvider");
		}
	}

	public <T> NamedDomainObjectProvider<T> create(NamedDomainObjectProviderSpec<T> spec) {
		return factory.create(spec);
	}

	@SuppressWarnings("unchecked")
	public <T> NamedDomainObjectProvider<T> notDefined() {
		return (NamedDomainObjectProvider<T>) factory.create(NamedDomainObjectProviderSpec.builder().named(() -> { throw new UnsupportedOperationException(); }).delegateTo(Providers.notDefined()).configureUsing(action -> { throw new UnsupportedOperationException(); }).build());
	}

	private static final class ProviderFactory {
		private final Constructor<?> constructor;

		private ProviderFactory(String className) {
			try {
				this.constructor = Class.forName(className).getConstructor(NamedDomainObjectProviderSpec.class);
			} catch (NoSuchMethodException | ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		@SuppressWarnings("unchecked")
		public <T> NamedDomainObjectProvider<T> create(NamedDomainObjectProviderSpec<T> spec) {
			try {
				return (NamedDomainObjectProvider<T>) constructor.newInstance(spec);
			} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
