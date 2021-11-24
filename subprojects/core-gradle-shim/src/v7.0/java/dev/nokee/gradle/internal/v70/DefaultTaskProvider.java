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
package dev.nokee.gradle.internal.v70;

import dev.nokee.gradle.NamedDomainObjectProviderSpec;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.Task;
import org.gradle.api.internal.provider.ProviderInternal;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.state.Managed;

import javax.annotation.Nullable;

public final class DefaultTaskProvider<T extends Task> implements Named, TaskProvider<T>
	, NamedDomainObjectProviderMixIn<T>
	, ProviderInternalMixIn<T>
	, ManagedMixIn
{
	private final NamedDomainObjectProviderSpec<T> spec;

	public DefaultTaskProvider(NamedDomainObjectProviderSpec<T> spec) {
		this.spec = spec;
	}

	@Override
	public Managed getManaged() {
		return (Managed) spec.getDelegate();
	}

	@Override
	public ProviderInternal<T> getDelegate() {
		return (ProviderInternal<T>) spec.getDelegate();
	}

	@Nullable
	@Override
	public Class<T> getType() {
		return spec.getType();
	}

	@Override
	public void configure(Action<? super T> action) {
		spec.getConfigurer().accept(action);
	}

	@Override
	public String getName() {
		return spec.getName();
	}
}
