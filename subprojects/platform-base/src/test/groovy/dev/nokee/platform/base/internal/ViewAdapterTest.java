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
import dev.nokee.internal.testing.reflect.Invokable;
import dev.nokee.internal.testing.reflect.MethodCallable;
import dev.nokee.internal.testing.testdoubles.TestDouble;
import dev.nokee.utils.ActionTestUtils;
import dev.nokee.utils.ClosureTestUtils;
import dev.nokee.utils.ClosureWrappedConfigureAction;
import dev.nokee.utils.SpecTestUtils;
import groovy.lang.Closure;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.function.Function;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.called;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.neverCalled;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.with;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.any;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newSpy;
import static dev.nokee.internal.testing.testdoubles.StubBuilder.WithArguments.args;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static dev.nokee.internal.testing.testdoubles.TestDoubleTypes.ofClosure;
import static dev.nokee.internal.testing.testdoubles.TestDoubleTypes.ofIterable;
import static dev.nokee.internal.testing.testdoubles.TestDoubleTypes.ofSpec;
import static dev.nokee.internal.testing.testdoubles.TestDoubleTypes.ofTransformer;
import static dev.nokee.internal.testing.util.ProjectTestUtils.providerFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ViewAdapterTest {
	private final TestDouble<ViewAdapter.Strategy> delegate = newMock(ViewAdapter.Strategy.class);
	private final ViewAdapter<MyType> subject = new ViewAdapter<>(MyType.class, delegate.instance());

	@Test
	void forwardsConfigureEachActionToStrategy() {
		val action = ActionTestUtils.<MyType>doSomething();
		subject.configureEach(action);
		assertThat(delegate.to(method(Strategy_configureEach(MyType.class))), calledOnceWith(MyType.class, action));
	}

	private static <T> MethodCallable.ForArg2<ViewAdapter.Strategy, Class<T>, Action<T>, RuntimeException> Strategy_configureEach(Class<T> type) {
		return (self, t, a) -> self.configureEach(t, a);
	}

	@Test
	void throwsExceptionIfConfigureEachActionIsNull() {
		assertThrows(NullPointerException.class, () -> subject.configureEach((Action<MyType>) null));
	}

	@Test
	void forwardsConfigureEachClosureToStrategy() {
		val closure = ClosureTestUtils.doSomething(MyType.class);
		subject.configureEach(closure);
		assertThat(delegate.to(method(Strategy_configureEach(MyType.class))),
			calledOnceWith(MyType.class, new ClosureWrappedConfigureAction<>(closure)));
	}

	@Test
	void throwsExceptionIfConfigureEachClosureIsNull() {
		assertThrows(NullPointerException.class, () -> subject.configureEach((Closure<?>) null));
	}

	@Test
	void forwardsConfigureEachActionByTypeToStrategy() {
		val action = ActionTestUtils.<MySubType>doSomething();
		subject.configureEach(MySubType.class, action);
		assertThat(delegate.to(method(Strategy_configureEach(MySubType.class))), calledOnceWith(MySubType.class, action));
	}

	@Test
	void forwardsConfigureEachClosureByTypeToStrategy() {
		val closure = ClosureTestUtils.doSomething(MySubType.class);
		subject.configureEach(MySubType.class, closure);
		assertThat(delegate.to(method(Strategy_configureEach(MySubType.class))),
			calledOnceWith(MySubType.class, new ClosureWrappedConfigureAction<>(closure)));
	}

	@Test
	void throwsExceptionIfConfigureEachByTypeClosureIsNull() {
		assertThrows(NullPointerException.class, () -> subject.configureEach(MyType.class, (Closure<?>) null));
	}

	@Test
	void forwardsConfigureEachActionBySpecToStrategy() {
		val action = ActionTestUtils.<MyType>doSomething();
		val spec = SpecTestUtils.aSpec();
		subject.configureEach(spec, action);
		assertThat(delegate.to(method(Strategy_configureEach(MyType.class))),
			calledOnceWith(MyType.class, new SpecFilteringAction<>(spec, action)));
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
		assertThat(delegate.to(method(Strategy_configureEach(MyType.class))),
			calledOnceWith(MyType.class, new SpecFilteringAction<>(spec, new ClosureWrappedConfigureAction<>(closure))));
	}

	@Test
	void throwsExceptionIfConfigureEachByClosureSpecIsNull() {
		assertThrows(NullPointerException.class, () -> subject.configureEach((Spec<MyType>) null, newSpy(ofClosure(MyType.class)).instance()));
	}

	@Test
	void throwsExceptionIfConfigureEachBySpecClosureIsNull() {
		assertThrows(NullPointerException.class, () -> subject.configureEach(SpecTestUtils.aSpec(), (Closure<?>) null));
	}

	@Test
	void returnsNewViewAdapterForSubType() {
		val result = new ViewAdapter<>(MySubType.class, delegate.instance());
		assertEquals(result, subject.withType(MySubType.class));
	}

	@Test
	void returnsSameViewAdapterForSameType() {
		assertEquals(subject, subject.withType(MyType.class));
	}

	@Test
	void forwardsGetElementsToStrategy() {
		@SuppressWarnings("unchecked") val result = (Provider<Set<MyType>>) mock(Provider.class);
		delegate.when(callTo(method(Strategy_getElements(MyType.class))).with(args(MyType.class)).then(doReturn(result)));
		assertEquals(result, subject.getElements());
		assertThat(delegate.to(method(Strategy_getElements(MyType.class))), calledOnceWith(MyType.class));
	}

	private static <T> MethodCallable.WithReturn.ForArg1<ViewAdapter.Strategy, Provider<Set<T>>, Class<T>, RuntimeException> Strategy_getElements(Class<T> type) {
		return (self, t) -> self.getElements(t);
	}

	@Test
	void resolvesElementsProviderFromStrategy() {
		Set<MyType> result = ImmutableSet.of(new MyType(), new MySubType());
		val elementsProvider = providerFactory().provider(() -> result);
		delegate.when(callTo(method(Strategy_getElements(MyType.class))).with(args(MyType.class)).then(doReturn(elementsProvider)));
		assertEquals(result, subject.get());
	}

	@Test
	@SuppressWarnings("rawtypes")
	void mapsElementsProviderFromStrategy() {
		val e0 = new MyType();
		val e1 = new MySubType();
		val e2 = new MyType();
		Provider<Set<MyType>> elementsProvider = providerFactory().provider(() -> ImmutableSet.of(e0, e1, e2));
		val mapper = newMock(ofTransformer(Class.class, MyType.class)).when(any(callTo(method(Transformer<Class, MyType>::transform))).then(doReturn((it, args) -> args.getArgument(0).getClass())));
		delegate.when(callTo(method(Strategy_getElements(MyType.class))).with(args(MyType.class)).then(doReturn(elementsProvider)));

		val result = subject.map(mapper.instance());
		assertNotNull(result);
		assertThat(mapper.to(method(Transformer<Class, MyType>::transform)), neverCalled());

		assertThat(result, providerOf(contains(MyType.class, MySubType.class, MyType.class)));
		assertThat(mapper.to(method(Transformer<Class, MyType>::transform)), called(with(e0), with(e1), with(e2)));
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
		val mapper = newMock(ofTransformer(ofIterable(Object.class), MyType.class))
			.when(any(callTo(method(Transformer<Iterable<Object>, MyType>::transform))).then(doReturn((it, args) -> ImmutableList.of(args.getArgument(0).getClass(), args.getArgument(0).getClass().getSimpleName()))));
		delegate.when(callTo(method(Strategy_getElements(MyType.class))).with(args(MyType.class)).then(doReturn(elementsProvider)));

		val result = subject.flatMap(mapper.instance());
		assertNotNull(result);
		assertThat(mapper.to(method(Transformer<Iterable<Object>, MyType>::transform)), neverCalled());

		assertThat(result, providerOf(contains(MyType.class, "MyType", MySubType.class, "MySubType", MyType.class, "MyType")));
		assertThat(mapper.to(method(Transformer<Iterable<Object>, MyType>::transform)), called(with(e0), with(e1), with(e2)));
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
		val spec = newMock(ofSpec(MyType.class))
			.when(any(callTo(method(Spec<MyType>::isSatisfiedBy))).then(doReturn(resultOf(MySubType.class::isInstance))));
		delegate.when(callTo(method(Strategy_getElements(MyType.class))).with(args(MyType.class)).then(doReturn(elementsProvider)));

		val result = subject.filter(spec.instance());
		assertNotNull(result);
		assertThat(spec.to(method(Spec<MyType>::isSatisfiedBy)), neverCalled());

		assertThat(result, providerOf(contains(e1)));
		assertThat(spec.to(method(Spec<MyType>::isSatisfiedBy)), called(with(e0), with(e1), with(e2)));
	}

	private static <ReceiverType, A0, ReturnType> Invokable<ReceiverType, ReturnType, RuntimeException> resultOf(Function<A0, ReturnType> function) {
		return (it, args) -> function.apply(args.getArgument(0));
	}

	@Test
	void throwsExceptionIfFilterSpecIsNull() {
		assertThrows(NullPointerException.class, () -> subject.filter(null));
	}

	private static class MyType {}
	private static final class MySubType extends MyType {}
}
