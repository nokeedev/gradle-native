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
package dev.nokee.buildadapter.xcode;

import dev.nokee.buildadapter.xcode.internal.components.GradleProjectTag;
import dev.nokee.buildadapter.xcode.internal.components.GradleSettingsTag;
import dev.nokee.buildadapter.xcode.internal.components.SettingsDirectoryComponent;
import dev.nokee.buildadapter.xcode.internal.components.XCProjectComponent;
import dev.nokee.buildadapter.xcode.internal.plugins.XCProjectLocator;
import dev.nokee.buildadapter.xcode.internal.rules.XCProjectsDiscoveryRule;
import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.of;
import static dev.nokee.buildadapter.xcode.TestProjectReference.project;
import static dev.nokee.model.fixtures.ModelEntityTestUtils.newEntity;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

@ExtendWith(MockitoExtension.class)
class XCProjectsDiscoveryRuleTests {
	@Mock XCProjectLocator locator;
	@Mock ModelRegistry registry;
	@InjectMocks XCProjectsDiscoveryRule subject;
	ModelNode settingsEntity = newEntity();

	@BeforeEach
	void createEntities() {
		settingsEntity.addComponent(tag(GradleSettingsTag.class));
		settingsEntity.addComponent(new SettingsDirectoryComponent(Paths.get("/test")));
	}

	@Nested
	class WhenNoProjectLocated {
		@BeforeEach
		void givenNoProjectLocated() {
			Mockito.when(locator.findProjects(Paths.get("/test"))).thenReturn(Collections.emptyList());
		}

		@Test
		void doesNotInstantiateAnyEntity() {
			subject.execute(settingsEntity);

			Mockito.verify(registry, never()).instantiate(any());
		}
	}

	@Nested
	class WhenMultipleProjectLocated {
		@BeforeEach
		void givenMultipleProjectLocated() {
			Mockito.when(locator.findProjects(Paths.get("/test")))
				.thenReturn(of(project("Foo"), project("Bar"), project("Far")));
		}

		@Test
		void instantiateEntitiesRepresentingEachLocatedProject() {
			subject.execute(settingsEntity);

			Mockito.verify(registry).instantiate(argThat(components(containsInAnyOrder(
				equalTo(tag(GradleProjectTag.class)),
				equalTo(new XCProjectComponent(project("Foo"))),
				equalTo(new ParentComponent(settingsEntity))))));
			Mockito.verify(registry).instantiate(argThat(components(containsInAnyOrder(
				equalTo(tag(GradleProjectTag.class)),
				equalTo(new XCProjectComponent(project("Bar"))),
				equalTo(new ParentComponent(settingsEntity))))));
			Mockito.verify(registry).instantiate(argThat(components(containsInAnyOrder(
				equalTo(tag(GradleProjectTag.class)),
				equalTo(new XCProjectComponent(project("Far"))),
				equalTo(new ParentComponent(settingsEntity))))));
		}
	}

	private static Matcher<ModelRegistration> components(Matcher<? super Iterable<ModelComponent>> matcher) {
		return new FeatureMatcher<ModelRegistration, Iterable<ModelComponent>>(matcher, "", "") {
			@Override
			protected Iterable<ModelComponent> featureValueOf(ModelRegistration actual) {
				return actual.getComponents().stream().map(ModelComponent.class::cast).collect(Collectors.toList());
			}
		};
	}
}
