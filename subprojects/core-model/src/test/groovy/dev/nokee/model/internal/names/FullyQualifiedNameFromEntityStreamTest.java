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

import static dev.nokee.model.internal.names.FullyQualifiedName.toFullyQualifiedName;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class FullyQualifiedNameFromEntityStreamTest {
	private final ModelNode parent = new ModelNode();
	private final ModelNode grandParent = new ModelNode();
	private final ModelNode greatGrandParent = new ModelNode();

	@Test
	void computesFullyQualifiedNameUsingElementNameOfEachParents() {
		parent.addComponent(new ElementNameComponent("bopu"));
		grandParent.addComponent(new ElementNameComponent("robo"));
		greatGrandParent.addComponent(new ElementNameComponent("zite"));

		assertThat(Stream.of(parent, grandParent, greatGrandParent).collect(toFullyQualifiedName(ElementName.of("ruba"))), equalTo(FullyQualifiedName.of("ziteRoboBopuRuba")));
	}

	@Test
	void ignoresEntityWithoutElementNameInFullyQualifiedName() {
		parent.addComponent(new ElementNameComponent("bopu"));
		// grand-parent has no element name
		greatGrandParent.addComponent(new ElementNameComponent("zite"));

		assertThat(Stream.of(parent, grandParent, greatGrandParent).collect(toFullyQualifiedName(ElementName.of("ruba"))), equalTo(FullyQualifiedName.of("ziteBopuRuba")));
	}

	@Test
	void ignoresMainEntityInFullyQualifiedName() {
		parent.addComponent(new ElementNameComponent("bopu"));
		grandParent.addComponent(new ElementNameComponent("robo"));
		grandParent.addComponent(tag(ExcludeFromQualifyingNameTag.class));
		greatGrandParent.addComponent(new ElementNameComponent("zite"));

		assertThat(Stream.of(parent, grandParent, greatGrandParent).collect(toFullyQualifiedName(ElementName.of("ruba"))), equalTo(FullyQualifiedName.of("ziteBopuRuba")));
	}

	@Test
	void ignoresEmptyElementNameInFullyQualfiedName() {
		parent.addComponent(new ElementNameComponent("bopu"));
		grandParent.addComponent(new ElementNameComponent("robo"));
		greatGrandParent.addComponent(new ElementNameComponent(""));

		assertThat(Stream.of(parent, grandParent, greatGrandParent).collect(toFullyQualifiedName(ElementName.of("ruba"))), equalTo(FullyQualifiedName.of("roboBopuRuba")));
	}
}
