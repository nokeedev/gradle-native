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
package dev.nokee.model.internal.names;

import dev.nokee.model.internal.core.ModelNode;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static dev.nokee.model.internal.names.ElementName.of;
import static dev.nokee.model.internal.names.RelativeNames.toRelativeNames;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

class RelativeNameFromEntityStreamTest {
	private final ModelNode parent = new ModelNode();
	private final ModelNode grandParent = new ModelNode();
	private final ModelNode greatGrandParent = new ModelNode();

	@Test
	void computesRelativeNameUsingElementNameOfEachParents() {
		parent.addComponent(new ElementNameComponent("tomo"));
		grandParent.addComponent(new ElementNameComponent("wova"));
		greatGrandParent.addComponent(new ElementNameComponent("buqe"));

		assertThat(Stream.of(parent, grandParent, greatGrandParent).collect(toRelativeNames(of("kles"))), containsInAnyOrder(RelativeName.of(parent, "kles"), RelativeName.of(grandParent, "tomoKles"), RelativeName.of(greatGrandParent, "wovaTomoKles")));
	}

	@Test
	void reusesRelativeNameForEntityWithoutElementNames() {
		parent.addComponent(new ElementNameComponent("tomo"));
		// grand-parent has no element name
		greatGrandParent.addComponent(new ElementNameComponent("buqe"));

		assertThat(Stream.of(parent, grandParent, greatGrandParent).collect(toRelativeNames(of("kles"))), containsInAnyOrder(RelativeName.of(parent, "kles"), RelativeName.of(grandParent, "tomoKles"), RelativeName.of(greatGrandParent, "tomoKles")));
	}

	@Test
	void reusesRelativeNameForMainEntity() {
		parent.addComponent(new ElementNameComponent("tomo"));
		grandParent.addComponent(new ElementNameComponent("wova"));
		grandParent.addComponent(tag(ExcludeFromQualifyingNameTag.class));
		greatGrandParent.addComponent(new ElementNameComponent("buqe"));

		assertThat(Stream.of(parent, grandParent, greatGrandParent).collect(toRelativeNames(of("kles"))), containsInAnyOrder(RelativeName.of(parent, "kles"), RelativeName.of(grandParent, "tomoKles"), RelativeName.of(greatGrandParent, "tomoKles")));
	}

	@Test
	void reusesRelativeNameForEntityWithEmptyElementName() {
		parent.addComponent(new ElementNameComponent(""));
		grandParent.addComponent(new ElementNameComponent("wova"));
		greatGrandParent.addComponent(new ElementNameComponent("buqe"));

		assertThat(Stream.of(parent, grandParent, greatGrandParent).collect(toRelativeNames(of("kles"))), containsInAnyOrder(RelativeName.of(parent, "kles"), RelativeName.of(grandParent, "kles"), RelativeName.of(greatGrandParent, "wovaKles")));
	}
}
