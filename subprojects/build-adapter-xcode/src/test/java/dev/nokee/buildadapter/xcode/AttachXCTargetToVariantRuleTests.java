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

import dev.nokee.buildadapter.xcode.internal.components.XCTargetComponent;
import dev.nokee.buildadapter.xcode.internal.rules.AttachXCTargetToVariantRule;
import dev.nokee.model.capabilities.variants.LinkedVariantsComponent;
import dev.nokee.model.internal.core.ModelNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.nokee.buildadapter.xcode.TestTargetReference.target;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
class AttachXCTargetToVariantRuleTests {
	@InjectMocks AttachXCTargetToVariantRule subject;
	ModelNode target = new ModelNode();
	ModelNode variant1 = new ModelNode();
	ModelNode variant2 = new ModelNode();

	@BeforeEach
	void givenTargetWithLinkedVariants() {
		target.addComponent(new LinkedVariantsComponent(asList(variant1, variant2)));
		target.addComponent(new XCTargetComponent(target("MyTarget")));
	}

	@Test
	void addsXCTargetComponentToEachLinkedVariants() {
		subject.execute(target);
		assertThat(variant1.get(XCTargetComponent.class), equalTo(new XCTargetComponent(target("MyTarget"))));
		assertThat(variant2.get(XCTargetComponent.class), equalTo(new XCTargetComponent(target("MyTarget"))));
	}
}