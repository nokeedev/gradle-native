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

import dev.nokee.buildadapter.xcode.internal.XcodeConfigurationParameter;
import dev.nokee.buildadapter.xcode.internal.components.XCTargetComponent;
import dev.nokee.buildadapter.xcode.internal.rules.XCTargetVariantDiscoveryRule;
import dev.nokee.model.capabilities.variants.KnownVariantInformationElement;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.platform.base.internal.IsComponent;
import dev.nokee.xcode.XCLoader;
import dev.nokee.xcode.XCTargetReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.nokee.buildadapter.xcode.TestTargetReference.target;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.model.fixtures.ModelEntityTestUtils.newEntity;
import static dev.nokee.model.internal.buffers.ModelBuffers.typeOf;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class XCTargetVariantDiscoveryRuleTests {
	@Mock XCLoader<Iterable<String>, XCTargetReference> configurationLoader;
	@Mock XcodeConfigurationParameter configuration;
	@InjectMocks XCTargetVariantDiscoveryRule subject;
	ModelNode target = newEntity();

	@BeforeEach
	void givenConfigurations() {
		Mockito.when(configurationLoader.load(any())).thenReturn(asList("Debug", "Release"));
		target.addComponent(new XCTargetComponent(target("MyTarget")));
		target.addComponent(tag(IsComponent.class));
	}

	@Nested
	class WhenNoSpecificConfigurationRequested {
		@Test
		void createsKnownVariantInformationForEachLoadedConfiguration() {
			subject.execute(target);
			assertThat(target.getComponent(typeOf(KnownVariantInformationElement.class)), contains(named("Debug"), named("Release")));
		}
	}

	@Nested
	class WhenSpecificConfigurationRequestedExists {
		@BeforeEach
		void givenRequestConfiguration() {
			Mockito.when(configuration.get()).thenReturn("Release");
		}

		@Test
		void createsKnownVariantInformationForRequestedConfigurationOnly() {
			subject.execute(target);
			assertThat(target.getComponent(typeOf(KnownVariantInformationElement.class)), contains(named("Release")));
		}
	}

	@Nested
	class WhenSpecificConfigurationRequestedDoesNotExists {
		@BeforeEach
		void givenRequestConfiguration() {
			Mockito.when(configuration.get()).thenReturn("rEleAse");
		}

		@Test
		void createsKnownVariantInformationForNoConfiguration() {
			subject.execute(target);
			assertThat(target.getComponent(typeOf(KnownVariantInformationElement.class)), emptyIterable());
		}
	}
}
