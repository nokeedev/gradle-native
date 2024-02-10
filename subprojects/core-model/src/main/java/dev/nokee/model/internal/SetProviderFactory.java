/*
 * Copyright 2024 the original author or authors.
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

import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

import java.util.Set;
import java.util.function.Consumer;

import static dev.nokee.utils.TransformerUtils.noOpTransformer;

public abstract class SetProviderFactory {
	public abstract <T> Provider<Set<T>> create(Consumer<? super Builder<T>> action);

	public interface Builder<T> {
		Builder<T> add(Provider<? extends T> provider);
	}

	public static SetProviderFactory forProject(Project project) {
		return new DefaultSetProviderFactory(project.getProviders(), project.getObjects());
	}

	private static final class DefaultSetProviderFactory extends SetProviderFactory {
		private final ProviderFactory providers;
		private final ObjectFactory objects;

		private DefaultSetProviderFactory(ProviderFactory providers, ObjectFactory objects) {
			this.providers = providers;
			this.objects = objects;
		}

		@Override
		@SuppressWarnings({"unchecked", "UnstableApiUsage"})
		public <T> Provider<Set<T>> create(Consumer<? super Builder<T>> action) {
			return providers.provider(() -> {
				final SetProperty<Object> result = objects.setProperty(Object.class);
				action.accept(new Builder<T>() {
					@Override
					public Builder<T> add(Provider<? extends T> provider) {
						result.add(provider);
						return this;
					}
				});

				return (Provider<? extends Set<T>>) result;
			}).flatMap(noOpTransformer());
		}
	}
}
