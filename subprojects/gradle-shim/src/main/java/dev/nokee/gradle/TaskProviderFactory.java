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

import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.util.GradleVersion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class TaskProviderFactory {
	private final ProviderFactory factory;

	public TaskProviderFactory() {
		if (GradleVersion.current().compareTo(GradleVersion.version("7.0")) >= 0) {
			this.factory = new ProviderFactory("dev.nokee.gradle.internal.v70.DefaultTaskProvider");
		} else if (GradleVersion.current().compareTo(GradleVersion.version("6.6")) >= 0) {
			this.factory = new ProviderFactory("dev.nokee.gradle.internal.v66.DefaultTaskProvider");
		} else if (GradleVersion.current().compareTo(GradleVersion.version("6.5")) >= 0) {
			this.factory = new ProviderFactory("dev.nokee.gradle.internal.v65.DefaultTaskProvider");
		} else if (GradleVersion.current().compareTo(GradleVersion.version("6.4")) >= 0) {
			this.factory = new ProviderFactory("dev.nokee.gradle.internal.v64.DefaultTaskProvider");
		} else {
			this.factory = new ProviderFactory("dev.nokee.gradle.internal.v62.DefaultTaskProvider");
		}
	}

	public <T extends Task> TaskProvider<T> create(NamedDomainObjectProviderSpec<T> spec) {
		return factory.create(spec);
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
		public <T extends Task> TaskProvider<T> create(NamedDomainObjectProviderSpec<T> spec) {
			try {
				return (TaskProvider<T>) constructor.newInstance(spec);
			} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
