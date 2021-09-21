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
package dev.nokee.model;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

public interface DomainObjectContainer<T> extends NamedDomainObjectView<T> {
	<U extends T> DomainObjectProvider<U> register(String name, Class<U> type);

	<U extends T> DomainObjectProvider<U> register(String name, Class<U> type, Action<? super U> action);

	default <U extends T> DomainObjectProvider<U> register(String name, Class<U> type, @DelegatesTo(type = "U", strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		return register(name, type, ConfigureUtil.configureUsing(closure));
	}

	<U extends T> void registerFactory(Class<U> type, DomainObjectFactory<? extends U> factory);

	<U extends T> void registerBinding(Class<U> type, Class<? extends U> implementationType);
}
