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
package dev.nokee.platform.base.testers;

import dev.nokee.internal.testing.testdoubles.TestClosure;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.dependencies.DependencyBuckets;
import groovy.lang.Closure;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ModuleDependency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static dev.nokee.internal.testing.ConfigurationMatchers.attributes;
import static dev.nokee.internal.testing.ConfigurationMatchers.dependencies;
import static dev.nokee.internal.testing.ConfigurationMatchers.module;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnce;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.withClosureArguments;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.withDelegateFirstStrategy;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.withDelegateOf;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newSpy;
import static dev.nokee.internal.testing.testdoubles.TestDoubleTypes.ofAction;
import static dev.nokee.internal.testing.testdoubles.TestDoubleTypes.ofClosure;
import static dev.nokee.platform.base.testers.DependencyUnderTest.externalDependency;
import static dev.nokee.platform.base.testers.DependencyUnderTest.projectDependency;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;
import static org.gradle.api.attributes.Attribute.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertAll;

public interface DependencyBucketTester<T> {
	T subject();

	DependencyBucket get(T self);
	void addDependency(T self, Object notation);
	void addDependency(T self, Object notation, Action<? super ModuleDependency> action);
	void addDependency(T self, Object notation, Closure closure);

	@Test
	default void canAccessDependencyBucketUsingTypeSafeAccessor() {
		assertThat(get(subject()), isA(DependencyBucket.class));
	}

	//region
	static Stream<DependencyUnderTest> provideDependencyNotations() {
		return Stream.of(
			projectDependency("com.example.foo", "foo", "1.0"),
			externalDependency("com.example.bar", "bar", "2.0").declaredAsMap(),
			externalDependency("com.example.far", "far", "3.0").declaredAsString()
		);
	}

	@ParameterizedTest(name = "canAddDependency()")
	@MethodSource("provideDependencyNotations")
	default void canAddDependency(DependencyUnderTest dependency) {
		addDependency(subject(), dependency.asNotation());
		assertThat(DependencyBuckets.finalize(get(subject()).getAsConfiguration()), dependencies(contains(dependency.asMatcher())));
	}
	//endregion

	//region
	@ParameterizedTest(name = "canAddAttributesToDependency()")
	@MethodSource("provideDependencyNotations")
	default void canAddAttributesToDependency(DependencyUnderTest dependency) {
		addDependency(subject(), dependency.asNotation(),
			configureAttributes(it -> it.attribute(of("com.example.attribute", String.class), "foo")));
		assertThat(DependencyBuckets.finalize(get(subject()).getAsConfiguration()).getDependencies(),
			contains(module(attributes(hasEntry(of("com.example.attribute", String.class), "foo")))));
	}
	//endregion

	//region
	@ParameterizedTest(name = "canConfigureDependencyViaTypeSafeMethodUsingAction()")
	@MethodSource("provideDependencyNotations")
	default void canConfigureDependencyViaTypeSafeMethodUsingAction(DependencyUnderTest dependency) {
		val action = newMock(ofAction(ModuleDependency.class));
		addDependency(subject(), dependency.asNotation(), action.instance());
		assertThat(DependencyBuckets.finalize(get(subject()).getAsConfiguration()).getDependencies(), hasSize(1)); // force realize
		assertThat(action.to(method(Action<ModuleDependency>::execute)), calledOnceWith(dependency.asMatcher()));
	}

	@ParameterizedTest(name = "canConfigureDependencyViaTypeSafeMethodUsingClosure()")
	@MethodSource("provideDependencyNotations")
	default void canConfigureDependencyViaTypeSafeMethodUsingClosure(DependencyUnderTest dependency) {
		val closure = newSpy(ofClosure(ModuleDependency.class));
		addDependency(subject(), dependency.asNotation(), closure.instance());
		assertThat(DependencyBuckets.finalize(get(subject()).getAsConfiguration()).getDependencies(), hasSize(1)); // force realize
		assertAll(
			() -> assertThat(closure.to(method(TestClosure<Object, ModuleDependency>::execute)), calledOnce(withClosureArguments(dependency.asMatcher()))),
			() -> assertThat(closure.to(method(TestClosure<Object, ModuleDependency>::execute)), calledOnce(allOf(withDelegateOf(dependency.asMatcher()), withDelegateFirstStrategy())))
		);
	}
	//endregion
}
