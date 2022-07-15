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

import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.junit.jupiter.GradleTestExtension;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.BucketDependencies;
import dev.nokee.platform.base.internal.dependencies.BucketDependenciesProperty;
import dev.nokee.platform.base.internal.dependencies.DependencyDefaultActionComponent;
import dev.nokee.platform.base.internal.dependencies.DependencyElement;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.testers.DependencyUnderTest;
import dev.nokee.utils.ActionTestUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ModuleDependency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.stream.Stream;

import static dev.nokee.internal.testing.ConfigurationMatchers.forCoordinate;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.platform.base.testers.DependencyUnderTest.externalDependency;
import static dev.nokee.platform.base.testers.DependencyUnderTest.projectDependency;
import static dev.nokee.utils.FunctionalInterfaceMatchers.calledOnceWith;
import static dev.nokee.utils.FunctionalInterfaceMatchers.neverCalled;
import static dev.nokee.utils.FunctionalInterfaceMatchers.singleArgumentOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;

@PluginRequirement.Require(type = ComponentModelBasePlugin.class)
@ExtendWith(GradleTestExtension.class)
class DependencyBucketDependenciesIntegrationTest {
	ModelNode subject;

	@BeforeEach
	void setUp(Project project) {
		subject = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(tag(IsDependencyBucket.class))
			.build());
	}

	@Test
	void hasBucketDependenciesProperty() {
		assertTrue(subject.has(BucketDependenciesProperty.class));
	}

	@Test
	void hasBucketDependenciesWhenBucketFinalized() {
		assertFalse(subject.has(BucketDependencies.class));
		assertTrue(ModelStates.finalize(subject).has(BucketDependencies.class));
	}

	@Test
	void syncsBucketDependenciesFromPropertyToComponentWhenFinalized() {
		ModelProperties.add(subject.get(BucketDependenciesProperty.class).get(), new DependencyElement("com.example:aaaa:6.7"));
		ModelProperties.add(subject.get(BucketDependenciesProperty.class).get(), new DependencyElement("com.example:bbbb:7.8"));
		assertThat(ModelStates.finalize(subject).get(BucketDependencies.class).get(), containsInAnyOrder(
			forCoordinate("com.example", "aaaa", "6.7"), forCoordinate("com.example", "bbbb", "7.8")));
	}

	@ParameterizedTest
	@MethodSource("provideModuleDependencyNotations")
	void usesDefaultActionForModuleDependencies(DependencyUnderTest dependency) {
		ModelProperties.add(subject.get(BucketDependenciesProperty.class).get(), new DependencyElement(dependency.asNotation()));

		val action = ActionTestUtils.mockAction(ModuleDependency.class);
		subject.addComponent(new DependencyDefaultActionComponent(action));

		// when:
		ModelStates.finalize(subject);

		// then:
		assertThat(action, calledOnceWith(singleArgumentOf(dependency.asMatcher())));
	}

	@Test
	void doesNotUsesDefaultActionForFileCollectionDependencies() {
		ModelProperties.add(subject.get(BucketDependenciesProperty.class).get(), new DependencyElement(objectFactory().fileCollection()));

		val action = ActionTestUtils.mockAction(ModuleDependency.class);
		subject.addComponent(new DependencyDefaultActionComponent(action));

		// when:
		ModelStates.finalize(subject);

		// then:
		assertThat(action, neverCalled());
	}

	@Test
	void callsDefaultActionBeforeModuleDependencyMutatorAction() {
		@SuppressWarnings("unchecked")
		val dependencyAction = (Action<ModuleDependency>) Mockito.mock(Action.class);
		ModelProperties.add(subject.get(BucketDependenciesProperty.class).get(), new DependencyElement("com.example:kels:6.4", dependencyAction));

		@SuppressWarnings("unchecked")
		val defaultAction = (Action<ModuleDependency>) Mockito.mock(Action.class);
		subject.addComponent(new DependencyDefaultActionComponent(defaultAction));

		// when:
		ModelStates.finalize(subject);

		// then:
		val inOrder = Mockito.inOrder(dependencyAction, defaultAction);
		inOrder.verify(defaultAction).execute(argThat(forCoordinate("com.example", "kels", "6.4")::matches));
		inOrder.verify(dependencyAction).execute(argThat(forCoordinate("com.example", "kels", "6.4")::matches));
	}

	static Stream<DependencyUnderTest> provideModuleDependencyNotations() {
		return Stream.of(
			projectDependency("com.example.foo", "foo", "1.0"),
			externalDependency("com.example.bar", "bar", "2.0").declaredAsMap(),
			externalDependency("com.example.far", "far", "3.0").declaredAsString()
		);
	}
}
