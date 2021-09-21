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

import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.ModelNode;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import java.util.function.Function;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.providerFactory;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

@Subject(ModelNodeBackedProvider.class)
public class ModelNodeBackedProviderTest {
	private <T> DomainObjectProvider<T> provider(Class<T> type, ModelNode node) {
		return new ModelNodeBackedProvider<>(of(type), node);
	}

	@Nested
	class CanGet extends AbstractDomainObjectFunctorTester<DomainObjectProvider> {
		private final MyType myTypeInstance = new MyType();
		private final MyOtherType myOtherTypeInstance = new MyOtherType();
		private final ModelNode node = node("foo", myTypeInstance, myOtherTypeInstance);

		protected <T> DomainObjectProvider<T> createSubject(Class<T> type) {
			return createSubject(type, node);
		}

		@Override
		protected <T> DomainObjectProvider createSubject(Class<T> type, ModelNode node) {
			return provider(type, node);
		}

		@Test
		void canGetDefaultProjectionViaProvider() {
			assertThat(createSubject(MyType.class).get(), equalTo(myTypeInstance));
		}

		@Test
		void canGetSecondaryProjectionViaProvider() {
			assertThat(createSubject(MyOtherType.class).get(), equalTo(myOtherTypeInstance));
		}

		@Test
		void realizeNodeWhenProviderIsRealized() {
			createSubject(MyType.class).get();
			assertThat(node.getState(), equalTo(ModelNode.State.Realized));
		}
	}

	@Nested
	class CanMap extends DomainObjectFunctorTransformTester<DomainObjectProvider> {
		@Override
		protected <T> DomainObjectProvider createSubject(Class<T> type, ModelNode node) {
			return provider(type, node);
		}

		@Override
		protected <T, R> Provider<R> transform(DomainObjectProvider functor, Function<? super T, ? extends R> mapper) {
			return ((DomainObjectProvider<T>)functor).map(mapper::apply);
		}
	}

	@Nested
	class CanFlatMap extends DomainObjectFunctorTransformTester<DomainObjectProvider> {
		@Override
		protected <T> DomainObjectProvider createSubject(Class<T> type, ModelNode node) {
			return provider(type, node);
		}

		@Override
		protected <T, R> Provider<R> transform(DomainObjectProvider functor, Function<? super T, ? extends R> mapper) {
			return ((DomainObjectProvider<T>)functor).flatMap(t -> providerFactory().provider(() -> mapper.apply(t)));
		}
	}

	@Nested
	class CanQueryType extends DomainObjectFunctorTypeAwarenessTester<DomainObjectProvider> {
		@Override
		protected <T> DomainObjectProvider createSubject(Class<T> type, ModelNode node) {
			return provider(type, node);
		}
	}

	@Nested
	class CheckIntegrity extends DomainObjectFunctorIntegrityTester<DomainObjectProvider> {
		@Override
		protected <T> DomainObjectProvider createSubject(Class<T> type, ModelNode node) {
			return provider(type, node);
		}

		@Override
		protected Class<DomainObjectProvider> getFunctorType() {
			return DomainObjectProvider.class;
		}
	}

	@Nested
	class CanConfigure extends DomainObjectFunctorConfigureTester<DomainObjectProvider> {
		@Override
		protected <T> DomainObjectProvider createSubject(Class<T> type, ModelNode node) {
			return provider(type, node);
		}
	}

	@Nested
	class CanQueryIdentifier extends DomainObjectFunctorIdentifierAwarenessTester<DomainObjectProvider> {
		@Override
		protected <T> DomainObjectProvider createSubject(Class<T> type, ModelNode node) {
			return provider(type, node);
		}
	}

	@Test
	void checkToString() {
		assertThat(provider(MyType.class, node("foo", new MyType())), hasToString("provider(node 'foo', class dev.nokee.model.internal.registry.ModelNodeBackedProviderTest$MyType)"));
		assertThat(provider(MyOtherType.class, node("bar", new MyOtherType())), hasToString("provider(node 'bar', class dev.nokee.model.internal.registry.ModelNodeBackedProviderTest$MyOtherType)"));
	}

	@Nested
	class CanAccessModelNode extends DomainObjectFunctorModelNodeTester<DomainObjectProvider> {
		@Override
		protected <T> DomainObjectProvider createSubject(Class<T> type) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected <T> DomainObjectProvider createSubject(Class<T> type, ModelNode node) {
			return provider(type, node);
		}
	}

	static class MyType {}
	static class MyOtherType {}
}
