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
package dev.nokee.model.internal.registry;

import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.KnownDomainObjectTester;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelProjections;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static dev.nokee.internal.testing.util.ProjectTestUtils.providerFactory;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;

class ModelNodeBackedKnownDomainObjectTest implements KnownDomainObjectTester<Object> {
	private final Object myTypeInstance = new Object();
	private final ModelNode node = node("foo", ModelProjections.ofInstance(myTypeInstance));
	private final ModelNodeBackedKnownDomainObject<Object> subject = new ModelNodeBackedKnownDomainObject<>(of(Object.class), node);

	private <T> KnownDomainObject<T> knownObject(Class<T> type, ModelNode node) {
		return new ModelNodeBackedKnownDomainObject<>(of(type), node);
	}

	@Override
	public KnownDomainObject<Object> subject() {
		return subject;
	}

	@Nested
	class CanMap extends DomainObjectFunctorTransformTester<KnownDomainObject> {

		@Override
		protected <T> KnownDomainObject createSubject(Class<T> type, ModelNode node) {
			return knownObject(type, node);
		}

		@Override
		protected <T, R> Provider<R> transform(KnownDomainObject functor, Function<? super T, ? extends R> mapper) {
			return ((KnownDomainObject<T>)functor).map(mapper::apply);
		}
	}

	@Nested
	class CanFlatMap extends DomainObjectFunctorTransformTester<KnownDomainObject> {

		@Override
		protected <T> KnownDomainObject createSubject(Class<T> type, ModelNode node) {
			return knownObject(type, node);
		}

		@Override
		protected <T, R> Provider<R> transform(KnownDomainObject functor, Function<? super T, ? extends R> mapper) {
			return ((KnownDomainObject<T>)functor).flatMap(t -> providerFactory().provider(() -> mapper.apply(t)));
		}
	}

	@Nested
	class CanQueryType extends DomainObjectFunctorTypeAwarenessTester<KnownDomainObject> {

		@Override
		protected <T> KnownDomainObject createSubject(Class<T> type, ModelNode node) {
			return knownObject(type, node);
		}
	}

	@Nested
	class CheckIntegrity extends DomainObjectFunctorIntegrityTester<KnownDomainObject> {
		@Override
		protected <T> KnownDomainObject createSubject(Class<T> type, ModelNode node) {
			return knownObject(type, node);
		}

		@Override
		protected Class<KnownDomainObject> getFunctorType() {
			return KnownDomainObject.class;
		}
	}

	@Nested
	class CanConfigure extends DomainObjectFunctorConfigureTester<KnownDomainObject> {

		@Override
		protected <T> KnownDomainObject createSubject(Class<T> type, ModelNode node) {
			return knownObject(type, node);
		}
	}

	@Nested
	class CanQueryIdentifier extends DomainObjectFunctorIdentifierAwarenessTester<KnownDomainObject> {
		@Override
		protected <T> KnownDomainObject createSubject(Class<T> type, ModelNode node) {
			return knownObject(type, node);
		}
	}

	@Test
	void checkToString() {
		assertThat(knownObject(MyType.class, node("foo", new MyType())), hasToString("known object(node 'foo', class dev.nokee.model.internal.registry.ModelNodeBackedKnownDomainObjectTest$MyType)"));
		assertThat(knownObject(MyOtherType.class, node("bar", new MyOtherType())), hasToString("known object(node 'bar', class dev.nokee.model.internal.registry.ModelNodeBackedKnownDomainObjectTest$MyOtherType)"));
	}

	@Nested
	class CanAccessModelNode extends DomainObjectFunctorModelNodeTester<KnownDomainObject> {
		@Override
		protected <T> KnownDomainObject createSubject(Class<T> type) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected <T> KnownDomainObject createSubject(Class<T> type, ModelNode node) {
			return knownObject(type, node);
		}
	}

	static class MyType {}
	static class MyOtherType {}
}
