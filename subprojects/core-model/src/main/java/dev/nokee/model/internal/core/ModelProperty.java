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
package dev.nokee.model.internal.core;

import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.type.ModelType;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.provider.HasConfigurableValue;

public interface ModelProperty<T> extends DomainObjectProvider<T> {
	/**
	 * {@inheritDoc}
	 */
	@Override
	ModelProperty<T> configure(Action<? super T> action);

	/**
	 * {@inheritDoc}
	 */
	@Override
	ModelProperty<T> configure(@SuppressWarnings("rawtypes") Closure closure);

	/**
	 * {@inheritDoc}
	 */
	@Override
	<S> ModelProperty<T> configure(ModelType<S> type, Action<? super S> action);

	/**
	 * Returns a Gradle property representation of this {@literal ModelProperty}.
	 *
	 * @param propertyType  the Gradle property type, i.e. SetProperty, must not be null
	 * @param <P>  the Gradle property type
	 * @return a Gradle property representing this {@literal ModelProperty}, never null
	 */
	<P extends HasConfigurableValue> P asProperty(ModelType<P> propertyType);
}
