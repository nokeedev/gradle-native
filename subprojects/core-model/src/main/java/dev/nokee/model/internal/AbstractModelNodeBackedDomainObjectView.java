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

import com.google.common.collect.Iterables;
import dev.nokee.model.DomainObjectView;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.type.ModelType;
import groovy.lang.Closure;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.internal.metaobject.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.TransformerUtils.*;
import static org.gradle.util.ConfigureUtil.configureUsing;

abstract class AbstractModelNodeBackedDomainObjectView<T> implements MethodMixIn, PropertyMixIn, DomainObjectView<T> {
	protected DynamicObject elementsDynamicObject;
	private final Projection projection;

	// Allow sub-implementation to access but not outside the package
	final ModelType<T> elementType;

	AbstractModelNodeBackedDomainObjectView(@Nullable ModelType<T> elementType, ModelNode node) {
		this(elementType, node, null); // TODO: Dynamic object just deny everything
	}

	AbstractModelNodeBackedDomainObjectView(@Nullable ModelType<T> elementType, ModelNode node, DynamicObject elementsDynamicObject) {
		if (elementType == null) {
			this.elementType = inferElementType(of(getClass()));
		} else {
			this.elementType = elementType;
		}
		this.elementsDynamicObject = elementsDynamicObject;
		this.projection = ModelNodeUtils.get(node, Projection.class);
	}

	private static <V, E> ModelType<E> inferElementType(ModelType<V> viewType) {
		val result = new ArrayList<ModelType<?>>();
		viewType.walkTypeHierarchy(new ModelType.Visitor<V>() {
			@Override
			public void visitType(ModelType<? super V> type) {
				if (type.getRawType().equals(DomainObjectView.class)) {
					result.addAll(type.getTypeVariables());
				}
			}
		});
		return (ModelType<E>) Iterables.getOnlyElement(result);
	}

	// TODO: Use GroovySupport directly
	@Override
	public final MethodAccess getAdditionalMethods() {
		return elementsDynamicObject;
	}

	@Override
	public final PropertyAccess getAdditionalProperties() {
		return elementsDynamicObject;
	}

	@Override
	public final void configureEach(Action<? super T> action) {
		projection.configureEach(elementType, action);
	}

	@Override
	public final void configureEach(Closure<Void> closure) {
		projection.configureEach(elementType, configureUsing(closure));
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Action<? super S> action) {
		projection.configureEach(of(type), action);
	}

	@Override
	public final <S extends T> void configureEach(Class<S> type, Closure<Void> closure) {
		configureEach(type, configureUsing(closure));
	}

	@Override
	public final void configureEach(Spec<? super T> spec, Action<? super T> action) {
		projection.configureEach(elementType, spec, action);
	}

	@Override
	public final void configureEach(Spec<? super T> spec, Closure<Void> closure) {
		projection.configureEach(elementType, spec, configureUsing(closure));
	}

	@Override
	public final Provider<Set<T>> getElements() {
		return projection.getElements(elementType);
	}

	@Override
	public final Set<T> get() {
		return getElements().get();
	}

	@Override
	public final <S> Provider<List<S>> map(Transformer<? extends S, ? super T> mapper) {
		return getElements().map(transformEach(mapper).andThen(toListTransformer()));
	}

	@Override
	public final <S> Provider<List<S>> flatMap(Transformer<? extends Iterable<S>, ? super T> mapper) {
		return getElements().map(flatTransformEach(mapper).andThen(toListTransformer()));
	}

	@Override
	public final Provider<List<T>> filter(Spec<? super T> spec) {
		return getElements().map(matching(spec).andThen(toListTransformer(elementType.getConcreteType())));
	}

	@Override
	public final void whenElementKnownEx(Action<? super KnownDomainObject<T>> action) {
		projection.whenElementKnown(elementType, action);
	}

	@Override
	public final void whenElementKnownEx(Closure<Void> closure) {
		projection.whenElementKnown(elementType, configureUsing(closure));
	}

	@Override
	public <S extends T> void whenElementKnownEx(Class<S> type, Action<? super KnownDomainObject<S>> action) {
		projection.whenElementKnown(of(type), action);
	}

	@Override
	public final <S extends T> void whenElementKnownEx(Class<S> type, Closure<Void> closure) {
		whenElementKnownEx(type, configureUsing(closure));
	}

	static <V> Object[] elementTypeParameter(ModelType<V> viewType, Class<?> viewInterfaceType) {
		// If the view/container doesn't take the element type as constructor argument
		if (Arrays.stream(viewType.getConcreteType().getDeclaredConstructors()).anyMatch(c -> c.getParameterCount() == 0)) {
			return new Object[0];
		}

		val result = new ArrayList<ModelType<?>>();
		viewType.walkTypeHierarchy(new ModelType.Visitor<V>() {
			@Override
			public void visitType(ModelType<? super V> type) {
				if (type.getRawType().equals(viewInterfaceType)) {
					result.addAll(type.getTypeVariables());
				}
			}
		});
		return new Object[] { Iterables.getOnlyElement(result).getConcreteType() };
	}

	interface Projection {
		<T> void configureEach(ModelType<T> type, Action<? super T> action);
		<T> void configureEach(ModelType<T> type, Spec<? super T> spec, Action<? super T> action);
		<T> Provider<Set<T>> getElements(ModelType<T> type);
		<T> void whenElementKnown(ModelType<T> type, Action<? super KnownDomainObject<T>> action);
		<T> DomainObjectView<T> createSubView(ModelType<T> type);
	}
}
