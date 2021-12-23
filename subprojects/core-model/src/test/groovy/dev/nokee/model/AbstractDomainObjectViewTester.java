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

import dev.nokee.model.internal.DefaultKnownDomainObject;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.type.ModelType;
import groovy.lang.Closure;
import lombok.val;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static dev.nokee.model.internal.core.ModelNodes.withParent;
import static dev.nokee.model.internal.core.ModelNodes.withType;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public abstract class AbstractDomainObjectViewTester<T> {
	private final TestViewGenerator<T> subjectGenerator = getSubjectGenerator();

	protected final ModelRegistry getModelRegistry() {
		return subjectGenerator.getModelRegistry();
	}

	protected final ModelLookup getModelLookup() {
		return subjectGenerator.getModelLookup();
	}

	protected final Class<? extends DomainObjectView<T>> getViewUnderTestType() {
		return subjectGenerator.getSubjectType();
	}

	protected final List<ModelNode> elementNodes(Predicate<ModelNode> predicate) {
		return subjectGenerator.getModelLookup().query(withParent(path("myTypes")).and(viewElements()).and(predicate)::test).get();
	}

	private Predicate<ModelNode> viewElements() {
		return withType(of(getElementType()));
	}

	protected abstract TestViewGenerator<T> getSubjectGenerator();

	protected DomainObjectView<T> createSubject() {
		return subjectGenerator.create("myTypes");
	}

	protected DomainObjectView<T> createSubject(String name) {
		return subjectGenerator.create(name);
	}

	protected final Class<T> getElementType() {
		return subjectGenerator.getElementType();
	}

	protected final <S extends T> Class<S> getSubElementType() {
		return subjectGenerator.getSubElementType();
	}



	//region TODO: Rewrite the following with better names
	// Get default projection for element name
	protected final Object e(String name) {
		return ModelNodeUtils.get(subjectGenerator.getModelLookup().get(path("myTypes." + name)), getElementType());
	}

	protected final Object e(String name, Class<?> type) {
		return ModelNodeUtils.get(subjectGenerator.getModelLookup().get(path("myTypes." + name)), type);
	}

	protected final KnownDomainObject<T> known(String name) {
		return DefaultKnownDomainObject.of(of(getElementType()), node(name));
	}

	protected final <S extends T> KnownDomainObject<S> known(String name, Class<S> type) {
		return DefaultKnownDomainObject.of(of(type), node(name));
	}

	protected final ModelNode node(String name) {
		return subjectGenerator.getModelLookup().get(path("myTypes." + name));
	}

	protected final void elements(String... names) {
		for (String name : names) {
			element(name);
		}
	}

	protected final DomainObjectProvider<T> element(String name) {
		return element(name, getElementType());
	}

	protected final <S> DomainObjectProvider<S> element(String name, Class<S> type) {
		return getModelRegistry().register(ModelRegistration.builder()
			.withComponent(ModelPath.path("myTypes." + name))
			.withComponent(ModelProjections.managed(ModelType.of(type)))
			.withComponent(ModelIdentifier.of("myTypes." + name, type))
			.build()
		).as(type);
	}

	protected final <S> DomainObjectProvider<S> register(String path, Class<S> type) {
		return getModelRegistry().register(ModelRegistration.builder()
			.withComponent(ModelPath.path(path))
			.withComponent(ModelProjections.managed(ModelType.of(type)))
			.withComponent(ModelIdentifier.of(path, type))
			.build()
		).as(type);
	}
	//endregion

	protected final <S> Closure<Void> adaptToClosure(Consumer<? super S> action) {
		return new Closure<Void>(new Object()) {
			public Void doCall(S t) {
				assertThat("delegate should be the first parameter", getDelegate(), equalTo(t));
				assertThat("resolve strategy should be delegate first", getResolveStrategy(), equalTo(Closure.DELEGATE_FIRST));
				action.accept(t);
				return null;
			}
		};
	}
}
