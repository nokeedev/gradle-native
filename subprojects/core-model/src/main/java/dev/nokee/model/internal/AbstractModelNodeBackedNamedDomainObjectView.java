/*
 * Copyright 2020-2021 the original author or authors.
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

import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.NamedDomainObjectView;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.type.ModelType;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.internal.metaobject.DynamicObject;

import javax.annotation.Nullable;
import java.util.Map;

import static dev.nokee.model.internal.type.ModelType.of;
import static org.gradle.util.ConfigureUtil.configureUsing;

abstract class AbstractModelNodeBackedNamedDomainObjectView<T> extends AbstractModelNodeBackedDomainObjectView<T> implements NamedDomainObjectView<T> {
	private final Projection projection;

	AbstractModelNodeBackedNamedDomainObjectView(@Nullable ModelType<T> elementType, ModelNode node) {
		this(elementType, node, null);
		elementsDynamicObject = new ModelNodeBackedNamedDomainObjectCollectionDynamicObject(this.elementType, node) {
			@Override
			protected boolean canRegister() {
				return false;
			}
		};
	}

	AbstractModelNodeBackedNamedDomainObjectView(@Nullable ModelType<T> elementType, ModelNode node, DynamicObject elementsDynamicObject) {
		super(elementType, node, elementsDynamicObject);
		this.projection = ModelNodeUtils.get(node, Projection.class);
	}

	@Override
	public final void configure(String name, Action<? super T> action) {
		projection.configure(name, elementType, action);
	}

	@Override
	public final void configure(String name, Closure<Void> closure) {
		projection.configure(name, elementType, configureUsing(closure));
	}

	@Override
	public <S extends T> void configure(String name, Class<S> type, Action<? super S> action) {
		projection.configure(name, of(type), action);
	}

	@Override
	public final <S extends T> void configure(String name, Class<S> type, Closure<Void> closure) {
		configure(name, type, configureUsing(closure));
	}

	@Override
	public final DomainObjectProvider<T> get(String name) {
		return projection.get(name, elementType);
	}

	@Override
	public final <S extends T> DomainObjectProvider<S> get(String name, Class<S> type) {
		return projection.get(name, of(type));
	}

	@Override
	public <S extends T> NamedDomainObjectProvider<S> named(String name, Class<S> type) {
		return projection.get(name, of(type)).asProvider();
	}

	interface Projection {
		<T> DomainObjectProvider<T> get(String name, ModelType<T> type);
		<T> void configure(String name, ModelType<T> type, Action<? super T> action);
		<T> Map<String, DomainObjectProvider<T>> getAsMap(ModelType<T> type);
		<T> NamedDomainObjectView<T> createSubView(ModelType<T> type);
	}
}
