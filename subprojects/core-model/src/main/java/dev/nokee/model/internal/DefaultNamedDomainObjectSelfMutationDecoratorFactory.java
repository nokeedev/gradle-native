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

import com.google.common.collect.Streams;
import dev.nokee.gradle.NamedDomainObjectProviderFactory;
import dev.nokee.gradle.TaskProviderFactory;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.internal.MutationGuard;
import org.gradle.api.internal.MutationGuards;
import org.gradle.api.tasks.TaskContainer;

import static dev.nokee.gradle.NamedDomainObjectProviderSpec.builder;

public final class DefaultNamedDomainObjectSelfMutationDecoratorFactory implements NamedDomainObjectProviderSelfMutationDecoratorFactory {
	@Override
	public NamedDomainObjectProviderDecorator forContainer(NamedDomainObjectContainer<?> container) {
		if (container instanceof TaskContainer) {
			return new DefaultTaskProviderDecorator(container);
		} else {
			return new DefaultNamedDomainObjectProviderDecorator(container);
		}
	}

	private static final class DefaultTaskProviderDecorator implements NamedDomainObjectProviderDecorator {
		private static final TaskProviderFactory PROVIDER_FACTORY = new TaskProviderFactory();
		private final MutationGuard guard;
		private final NamedDomainObjectContainer<?> container;

		public DefaultTaskProviderDecorator(NamedDomainObjectContainer<?> container) {
			this.guard = MutationGuards.of(container);
			this.container = container;
		}

		@Override
		@SuppressWarnings({"unchecked", "rawtypes"})
		public <T> NamedDomainObjectProvider<T> decorate(NamedDomainObjectProvider<T> provider) {
			if (Streams.stream(container.getCollectionSchema().getElements()).noneMatch(it -> it.getName().equals(provider.getName()))) {
				throw new UnsupportedOperationException(String.format("Cannot decorate %s.", provider));
			}
			val configureStrategy = new NamedDomainObjectProviderSelfMutationConfigureStrategy(guard);
			return PROVIDER_FACTORY.create(builder().delegateTo((NamedDomainObjectProvider) provider).configureUsing(a -> configureStrategy.configure(provider, (Action) a)).build());
		}
	}

	private static final class DefaultNamedDomainObjectProviderDecorator implements NamedDomainObjectProviderDecorator {
		private static final NamedDomainObjectProviderFactory PROVIDER_FACTORY = new NamedDomainObjectProviderFactory();
		private final MutationGuard guard;
		private final NamedDomainObjectContainer<?> container;

		public DefaultNamedDomainObjectProviderDecorator(NamedDomainObjectContainer<?> container) {
			this.guard = MutationGuards.of(container);
			this.container = container;
		}

		@Override
		@SuppressWarnings({"unchecked", "rawtypes"})
		public <T> NamedDomainObjectProvider<T> decorate(NamedDomainObjectProvider<T> provider) {
			if (Streams.stream(container.getCollectionSchema().getElements()).noneMatch(it -> it.getName().equals(provider.getName()))) {
				throw new UnsupportedOperationException(String.format("Cannot decorate %s.", provider));
			}
			val configureStrategy =  new NamedDomainObjectProviderSelfMutationConfigureStrategy(guard);
			return PROVIDER_FACTORY.create(builder().delegateTo((NamedDomainObjectProvider) provider).configureUsing(a -> configureStrategy.configure(provider, (Action) a)).build());
		}
	}
}
