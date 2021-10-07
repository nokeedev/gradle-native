/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.model.internal.dsl;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.DomainObjectConfigurer;
import dev.nokee.model.internal.DomainObjectRegistry;
import dev.nokee.model.internal.RealizableDomainObjectRepository;
import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import org.gradle.util.ConfigureUtil;

public class GroovyDslContainerInvoker<TYPE, T extends TYPE> extends AbstractGroovyDslInvoker<TYPE, T> implements GroovyDslInvoker<T> {
	private final DomainObjectIdentifier owner;
	private final DomainObjectConfigurer<TYPE> configurer;
	private final DomainObjectRegistry<T> registry;

	public GroovyDslContainerInvoker(GroovyObject object, DomainObjectIdentifier owner, Class<T> entityType, RealizableDomainObjectRepository<TYPE> repository, DomainObjectConfigurer<TYPE> configurer, DomainObjectRegistry<T> registry) {
		super(object, owner, entityType, repository);
		this.owner = owner;
		this.configurer = configurer;
		this.registry = registry;
	}

	@Override
	protected <S extends T> Object register(String name, Class<S> type) {
		return registry.register(name, type);
	}

	@Override
	protected <S extends T> Object register(String name, Class<S> type, @SuppressWarnings("rawtypes") Closure closure) {
		return registry.register(name, type, ConfigureUtil.configureUsing(closure));
	}

	@Override
	protected <S extends T> void configure(String name, Class<S> type, @SuppressWarnings("rawtypes") Closure closure) {
		configurer.configure(owner, name, type, ConfigureUtil.configureUsing(closure));
	}
}
