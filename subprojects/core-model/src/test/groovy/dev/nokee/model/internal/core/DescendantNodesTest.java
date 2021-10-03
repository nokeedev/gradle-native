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

package dev.nokee.model.internal.core;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelLookupDefaultResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.*;

class DescendantNodesTest {
	private final ModelLookup lookup = Mockito.mock(ModelLookup.class);
	private final ModelPath path = ModelPath.path("dker.lske");
	private final DescendantNodes subject = new DescendantNodes(lookup, path);
	private final ModelNode erds = new ModelNode();
	private final ModelNode klrs = new ModelNode();

	@BeforeEach
	void setUp() {
		Mockito.when(lookup.get(path.child("erds"))).thenReturn(erds);
		Mockito.when(lookup.get(path.child("klrs"))).thenReturn(klrs);
		Mockito.when(lookup.has(path.child("klrs"))).thenReturn(true);
		Mockito.when(lookup.has(path.child("potr"))).thenReturn(false);
		Mockito.when(lookup.query(allDirectDescendants().scope(path)))
			.thenReturn(new ModelLookupDefaultResult(ImmutableList.of(erds, klrs)));
	}

	@Test
	void canGetDescendantNode() {
		assertEquals(erds, subject.getDescendant("erds"));
	}

	@Test
	void hasDescendantNode() {
		assertTrue(subject.hasDescendant("klrs"));
		assertFalse(subject.hasDescendant("potr"));
	}

	@Test
	void canGetAllDirectlyDescendantNodes() {
		assertThat(subject.getDirectDescendants(), contains(erds, klrs));
	}
}
