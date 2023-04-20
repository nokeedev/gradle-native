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

import dev.nokee.buildadapter.xcode.internal.components.XCProjectComponent;
import dev.nokee.buildadapter.xcode.internal.components.XCTargetComponent;
import dev.nokee.buildadapter.xcode.internal.rules.XCTargetComponentDiscoveryRule;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.IsComponent;
import dev.nokee.xcode.XCLoader;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCTargetReference;
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

import java.util.Collections;
import java.util.stream.Collectors;

import static dev.nokee.buildadapter.xcode.TestProjectReference.project;
import static dev.nokee.buildadapter.xcode.TestTargetReference.target;
import static dev.nokee.model.fixtures.ModelEntityTestUtils.newEntity;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

@ExtendWith(MockitoExtension.class)
class XCTargetComponentDiscoveryRuleTests {
	@Mock ModelRegistry registry;
	@Mock XCLoader<Iterable<XCTargetReference>, XCProjectReference> loader;
	@InjectMocks XCTargetComponentDiscoveryRule subject;
	ModelNode target = newEntity();

	@BeforeEach
	void givenValidTarget() {
		target.addComponent(new DisplayNameComponent("project ':my-project'"));
		target.addComponent(new XCProjectComponent(project("MyProject")));
	}

	@Nested
	class WhenProjectHasNoTargets {
		@BeforeEach
		void givenProjectWithNoTargets() {
			Mockito.when(loader.load(any())).thenReturn(Collections.emptyList());
			subject.execute(target);
		}

		@Test
		void doesNotCreateEntities() {
			verify(registry, never()).instantiate(any());
		}
	}

	@Nested
	class WhenProjectHasTargets {
		ModelNode componentA = newEntity();
		ModelNode componentB = newEntity();

		@BeforeEach
		void givenProjectWithTargets() {
			Mockito.when(loader.load(any())).thenReturn(asList(target("A"), target("B")));
			Mockito.when(registry.instantiate(any())).thenReturn(componentA, componentB);
			subject.execute(target);
		}

		@Test
		void createsEntityForEachXcodeTarget() {
			verify(registry).instantiate(argThat(components(containsInAnyOrder(
				equalTo(new ParentComponent(target)),
				equalTo(new ElementNameComponent("A")),
				equalTo(tag(IsComponent.class)),
				equalTo(new DisplayNameComponent("target 'A' of project ':my-project'")),
				equalTo(new XCTargetComponent(target("A")))
			))));
			verify(registry).instantiate(argThat(components(containsInAnyOrder(
				equalTo(new ParentComponent(target)),
				equalTo(new ElementNameComponent("B")),
				equalTo(tag(IsComponent.class)),
				equalTo(new DisplayNameComponent("target 'B' of project ':my-project'")),
				equalTo(new XCTargetComponent(target("B")))
			))));
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
