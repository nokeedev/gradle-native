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
package dev.nokee.model.capabilities.variants;

import dev.nokee.model.internal.buffers.ModelBuffers;
import dev.nokee.model.internal.core.ModelAction;
import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Collectors;

import static dev.nokee.model.internal.tags.ModelTags.tag;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

@ExtendWith(MockitoExtension.class)
class CreateVariantsRuleTests {
	@Mock ModelRegistry registry;
	ModelAction subject;
	ModelNode target = new ModelNode();

	@BeforeEach
	void createSubject() {
		subject = new CreateVariantsRule(registry);
	}

	@Nested
	class WhenVariantsAreKnown {
		ModelNode variantA = new ModelNode();
		ModelNode variantB = new ModelNode();

		@BeforeEach
		void givenKnownVariants() {
			target.addComponent(ModelBuffers.of(new KnownVariantInformationElement("a"), new KnownVariantInformationElement("b")));

			Mockito.when(registry.instantiate(any())).thenReturn(variantA, variantB);
			subject.execute(target);
		}

		@Test
		void createsChildEntityForEachKnownVariants() {
			Mockito.verify(registry).instantiate(argThat(components(containsInAnyOrder(
				equalTo(tag(IsVariant.class)),
				equalTo(new ElementNameComponent("a")),
				equalTo(new ParentComponent(target)),
				equalTo(new VariantInformationComponent("a"))
			))));
			Mockito.verify(registry).instantiate(argThat(components(containsInAnyOrder(
				equalTo(tag(IsVariant.class)),
				equalTo(new ElementNameComponent("b")),
				equalTo(new ParentComponent(target)),
				equalTo(new VariantInformationComponent("b"))
			))));
		}

		@Test
		void linksVariantsOnTargetEntity() {
			assertThat(target.get(LinkedVariantsComponent.class), contains(variantA, variantB));
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
