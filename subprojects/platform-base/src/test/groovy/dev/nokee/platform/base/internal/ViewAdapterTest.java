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
package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.utils.*;
import groovy.lang.Closure;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.util.ProjectTestUtils.providerFactory;
import static dev.nokee.utils.FunctionalInterfaceMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ViewAdapterTest {
	private final ViewAdapter.Strategy delegate = Mockito.mock(ViewAdapter.Strategy.class);
	private final ViewAdapter<MyType> subject = new ViewAdapter<>(MyType.class, delegate);

	@Test
	void forwardsConfigureEachActionToStrategy() {
		val action = ActionTestUtils.doSomething();
		subject.configureEach(action);
		verify(delegate).configureEach(MyType.class, action);
	}

	@Test
	void forwardsConfigureEachClosureToStrategy() {
		val closure = ClosureTestUtils.doSomething(MyType.class);
		subject.configureEach(closure);
		verify(delegate).configureEach(MyType.class, new ClosureWrappedConfigureAction<>(closure));
	}

	@Test
	void throwsExceptionIfConfigureEachClosureIsNull() {
		assertThrows(NullPointerException.class, () -> subject.configureEach((Closure<?>) null));
	}

	@Test
	void forwardsConfigureEachActionByTypeToStrategy() {
		val action = ActionTestUtils.doSomething();
		subject.configureEach(MySubType.class, action);
		verify(delegate).configureEach(MySubType.class, action);
	}

	@Test
	void forwardsConfigureEachClosureByTypeToStrategy() {
		val closure = ClosureTestUtils.doSomething(MySubType.class);
		subject.configureEach(MySubType.class, closure);
		verify(delegate).configureEach(MySubType.class, new ClosureWrappedConfigureAction<>(closure));
	}

	@Test
	void throwsExceptionIfConfigureEachByTypeClosureIsNull() {
		assertThrows(NullPointerException.class, () -> subject.configureEach(MyType.class, (Closure<?>) null));
	}

	@Test
	void forwardsConfigureEachActionBySpecToStrategy() {
		val action = ActionTestUtils.doSomething();
		val spec = SpecTestUtils.aSpec();
		subject.configureEach(spec, action);
		verify(delegate).configureEach(MyType.class, new SpecFilteringAction<>(spec, action));
	}

	@Test
	void throwsExceptionIfConfigureEachByActionSpecIsNull() {
		assertThrows(NullPointerException.class, () -> subject.configureEach((Spec<MyType>) null, ActionTestUtils.doSomething()));
	}

	@Test
	void throwsExceptionIfConfigureEachBySpecActionIsNull() {
		assertThrows(NullPointerException.class, () -> subject.configureEach(SpecTestUtils.aSpec(), (Action<MyType>) null));
	}

	@Test
	void forwardsConfigureEachClosureBySpecToStrategy() {
		val closure = ClosureTestUtils.doSomething(MyType.class);
		val spec = SpecTestUtils.aSpec();
		subject.configureEach(spec, closure);
		verify(delegate).configureEach(MyType.class, new SpecFilteringAction<>(spec, new ClosureWrappedConfigureAction<>(closure)));
	}

	@Test
	void throwsExceptionIfConfigureEachByClosureSpecIsNull() {
		assertThrows(NullPointerException.class, () -> subject.configureEach((Spec<MyType>) null, ClosureTestUtils.mockClosure(MyType.class)));
	}

	@Test
	void throwsExceptionIfConfigureEachBySpecClosureIsNull() {
		assertThrows(NullPointerException.class, () -> subject.configureEach(SpecTestUtils.aSpec(), (Closure<?>) null));
	}

	@Test
	void returnsNewViewAdapterForSubType() {
		val result = new ViewAdapter<>(MySubType.class, delegate);
		assertEquals(result, subject.withType(MySubType.class));
	}

	@Test
	void returnsSameViewAdapterForSameType() {
		assertEquals(subject, subject.withType(MyType.class));
	}

	@Test
	void forwardsGetElementsToStrategy() {
		@SuppressWarnings("unchecked") val result = (Provider<Set<MyType>>) mock(Provider.class);
		when(delegate.getElements(MyType.class)).thenReturn(result);
		assertEquals(result, subject.getElements());
		verify(delegate).getElements(MyType.class);
	}

	@Test
	void resolvesElementsProviderFromStrategy() {
		Set<MyType> result = ImmutableSet.of(new MyType(), new MySubType());
		val elementsProvider = providerFactory().provider(() -> result);
		when(delegate.getElements(MyType.class)).thenReturn(elementsProvider);
		assertEquals(result, subject.get());
	}

	@Test
	void mapsElementsProviderFromStrategy() {
		val e0 = new MyType();
		val e1 = new MySubType();
		val e2 = new MyType();
		Provider<Set<MyType>> elementsProvider = providerFactory().provider(() -> ImmutableSet.of(e0, e1, e2));
		val mapper = TransformerTestUtils.<Class<?>, MyType>mockTransformer().whenCalled(Object::getClass);
		when(delegate.getElements(MyType.class)).thenReturn(elementsProvider);

		val result = subject.map(mapper);
		assertNotNull(result);
		assertThat(mapper, neverCalled());

		assertThat(result, providerOf(contains(MyType.class, MySubType.class, MyType.class)));
		assertThat(mapper, calledWith(contains(singleArgumentOf(e0), singleArgumentOf(e1), singleArgumentOf(e2))));
	}

	@Test
	void throwsExceptionIfMapTransformIsNull() {
		assertThrows(NullPointerException.class, () -> subject.map(null));
	}

	@Test
	void flatMapsElementsProviderFromStrategy() {
		val e0 = new MyType();
		val e1 = new MySubType();
		val e2 = new MyType();
		Provider<Set<MyType>> elementsProvider = providerFactory().provider(() -> ImmutableSet.of(e0, e1, e2));
		val mapper = TransformerTestUtils.<Iterable<Object>, MyType>mockTransformer()
			.whenCalled(it -> ImmutableList.of(it.getClass(), it.getClass().getSimpleName()));
		when(delegate.getElements(MyType.class)).thenReturn(elementsProvider);

		val result = subject.flatMap(mapper);
		assertNotNull(result);
		assertThat(mapper, neverCalled());

		assertThat(result, providerOf(contains(MyType.class, "MyType", MySubType.class, "MySubType", MyType.class, "MyType")));
		assertThat(mapper, calledWith(contains(singleArgumentOf(e0), singleArgumentOf(e1), singleArgumentOf(e2))));
	}

	@Test
	void throwsExceptionIfFlatMapTransformIsNull() {
		assertThrows(NullPointerException.class, () -> subject.flatMap(null));
	}

	@Test
	void filtersElementsProviderFromStrategy() {
		val e0 = new MyType();
		val e1 = new MySubType();
		val e2 = new MyType();
		Provider<Set<MyType>> elementsProvider = providerFactory().provider(() -> ImmutableSet.of(e0, e1, e2));
		val spec = SpecTestUtils.mockSpec()
			.whenCalled(MySubType.class::isInstance);
		when(delegate.getElements(MyType.class)).thenReturn(elementsProvider);

		val result = subject.filter(spec);
		assertNotNull(result);
		assertThat(spec, neverCalled());

		assertThat(result, providerOf(contains(e1)));
		assertThat(spec, calledWith(contains(singleArgumentOf(e0), singleArgumentOf(e1), singleArgumentOf(e2))));
	}

	@Test
	void throwsExceptionIfFilterSpecIsNull() {
		assertThrows(NullPointerException.class, () -> subject.filter(null));
	}

	private static class MyType {}
	private static final class MySubType extends MyType {}
}
