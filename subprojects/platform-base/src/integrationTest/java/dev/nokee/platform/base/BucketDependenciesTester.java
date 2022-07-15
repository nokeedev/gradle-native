/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.platform.base;

import dev.nokee.platform.base.testers.DependencyUnderTest;
import dev.nokee.utils.ActionTestUtils;
import lombok.val;
import org.gradle.api.artifacts.ModuleDependency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.stream.Stream;

import static dev.nokee.internal.testing.ConfigurationMatchers.forCoordinate;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.internal.testing.util.ProjectTestUtils.providerFactory;
import static dev.nokee.platform.base.testers.DependencyUnderTest.externalDependency;
import static dev.nokee.platform.base.testers.DependencyUnderTest.filesDependency;
import static dev.nokee.platform.base.testers.DependencyUnderTest.projectDependency;
import static dev.nokee.utils.ActionTestUtils.doSomething;
import static dev.nokee.utils.FunctionalInterfaceMatchers.calledOnceWith;
import static dev.nokee.utils.FunctionalInterfaceMatchers.neverCalled;
import static dev.nokee.utils.FunctionalInterfaceMatchers.singleArgumentOf;
import static dev.nokee.utils.ProviderUtils.resolve;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public interface BucketDependenciesTester {
	DependencyBucket subject();

	static Stream<DependencyUnderTest> provideModuleDependencyNotations() {
		return Stream.of(
			projectDependency("com.example.foo", "foo", "1.0"),
			externalDependency("com.example.bar", "bar", "2.0").declaredAsMap(),
			externalDependency("com.example.far", "far", "3.0").declaredAsString()
		);
	}

	static Stream<DependencyUnderTest> provideDependencyNotations() {
		return Stream.of(
			filesDependency(new File("har").toPath().toAbsolutePath().toFile(), new File("gar").toPath().toAbsolutePath().toFile()),
			projectDependency("com.example.foo", "foo", "1.0"),
			externalDependency("com.example.bar", "bar", "2.0").declaredAsMap(),
			externalDependency("com.example.far", "far", "3.0").declaredAsString()
		);
	}

	@ParameterizedTest(name = "canAddDependency() [{arguments}]")
	@MethodSource("provideDependencyNotations")
	default void canAddDependency(DependencyUnderTest dependency) {
		subject().addDependency(dependency.asNotation());
		assertThat(subject().getDependencies(), providerOf(hasItem(dependency.asMatcher())));
	}

	@ParameterizedTest(name = "canAddDependencyOfProviderNotation() [{arguments}]")
	@MethodSource("provideDependencyNotations")
	default void canAddDependencyOfProviderNotation(DependencyUnderTest dependency) {
		subject().addDependency(providerFactory().provider(dependency::asNotation));
		assertThat(subject().getDependencies(), providerOf(hasItem(dependency.asMatcher())));
	}

	@ParameterizedTest(name = "canAddDependencyWithAction() [{arguments}]")
	@MethodSource("provideModuleDependencyNotations")
	default void canAddDependencyWithAction(DependencyUnderTest dependency) {
		subject().addDependency(dependency.asNotation(), doSomething());
		assertThat(subject().getDependencies(), providerOf(hasItem(dependency.asMatcher())));
	}

	@ParameterizedTest(name = "canAddDependencyOfProviderNotationWithAction() [{arguments}]")
	@MethodSource("provideModuleDependencyNotations")
	default void canAddDependencyOfProviderNotationWithAction(DependencyUnderTest dependency) {
		subject().addDependency(providerFactory().provider(dependency::asNotation), doSomething());
		assertThat(subject().getDependencies(), providerOf(hasItem(dependency.asMatcher())));
	}

	@Test
	default void throwsExceptionWhenAddingDependencyOfFileCollectionNotationWithAction() {
		assertThrows(IllegalArgumentException.class, () -> subject().addDependency(objectFactory().fileCollection(), doSomething()));
	}

	@Test
	default void throwsExceptionWhenResolvingProviderOfFileCollectionNotationWithAction() {
		assertDoesNotThrow(() -> subject().addDependency(providerFactory().provider(objectFactory()::fileCollection), doSomething()));
		assertThrows(IllegalArgumentException.class, () -> resolve(subject().getDependencies()));
	}

	@Test
	default void executesActionOnlyWhenDependenciesRealized() {
		val action = ActionTestUtils.mockAction(ModuleDependency.class);
		subject().addDependency("com.example:peto:4.5", action);
		assertThat(action, neverCalled());
		resolve(subject().getDependencies());
		assertThat(action, calledOnceWith(singleArgumentOf(forCoordinate("com.example", "peto", "4.5"))));
	}
}
