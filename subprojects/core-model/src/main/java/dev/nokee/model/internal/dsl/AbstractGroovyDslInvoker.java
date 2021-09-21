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
import dev.nokee.model.internal.DomainObjectIdentifierUtils;
import dev.nokee.model.internal.RealizableDomainObjectRepository;
import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.Tuple;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.directlyOwnedBy;
import static dev.nokee.model.internal.DomainObjectIdentifierUtils.named;

public abstract class AbstractGroovyDslInvoker<TYPE, T extends TYPE> {
	private static final Object[] EMPTY_ARRAY = {};
	private final DomainObjectIdentifier owner;
	private final Class<T> entityType;
	private final RealizableDomainObjectRepository<TYPE> repository;
	private final GroovyObject object;

	protected AbstractGroovyDslInvoker(GroovyObject object, DomainObjectIdentifier owner, Class<T> entityType, RealizableDomainObjectRepository<TYPE> repository) {
		this.owner = owner;
		this.entityType = entityType;
		this.repository = repository;
		this.object = object;
	}

	protected final Object forwardMissingMethodInvocation(String methodName, Object arguments) {
		return object.getMetaClass().invokeMethod(object, methodName, arguments);
	}

	public final Object invokeMethod(String methodName, Object arguments) {
		if (arguments == null) {
			return invokeMethod(methodName, EMPTY_ARRAY);
		}
		if (arguments instanceof Tuple) {
			Tuple<?> tuple = (Tuple<?>) arguments;
			return invokeMethod(methodName, tuple.toArray());
		}
		if (arguments instanceof Object[]) {
			return invokeMethod(methodName, (Object[]) arguments);
		} else {
			return invokeMethod(methodName, new Object[]{arguments});
		}
	}

	private Object invokeMethod(String methodName, Object[] arguments) {
		if (arguments.length == 1 && arguments[0] instanceof Class) {
			return register(methodName, (Class) arguments[0]);
		} else if (arguments.length == 1 && arguments[0] instanceof Closure && hasEntity(methodName, entityType)) {
			configure(methodName, entityType, (Closure<Void>) arguments[0]);
			return null;
		} else if (arguments.length == 2 && arguments[0] instanceof Class && arguments[1] instanceof Closure) {
			if (hasEntity(methodName, (Class) arguments[0])) {
				configure(methodName, (Class) arguments[0], (Closure<Void>) arguments[1]);
				return null;
			}
			return register(methodName, (Class) arguments[0], (Closure<Void>) arguments[1]);
		}
		return forwardMissingMethodInvocation(methodName, arguments);
	}

	private boolean hasEntity(String methodName, Class<?> type) {
		return repository.anyKnownIdentifier(directlyOwnedBy(owner).and(named(methodName)).and(DomainObjectIdentifierUtils.withType(type)));
	}

	protected abstract <S extends T> Object register(String name, Class<S> type);

	protected abstract <S extends T> Object register(String name, Class<S> type, Closure<Void> closure);

	protected abstract <S extends T> void configure(String name, Class<S> type, Closure<Void> closure);
}
