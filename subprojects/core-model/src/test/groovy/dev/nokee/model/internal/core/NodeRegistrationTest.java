/*
 * Copyright 2020-2021 the original author or authors.
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

import com.google.common.testing.EqualsTester;
import dev.nokee.model.internal.state.ModelState;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.Factories.alwaysThrow;
import static dev.nokee.model.internal.core.ModelActions.matching;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelTestActions.doSomething;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

class NodeRegistrationTest {
	@Test
	void canCreateRegistrationOfManagedType() {
		val registration = NodeRegistration.of("c", of(MyType.class)).scope(path("a.b"));
		assertAll(() -> {
			assertThat(registration.getComponents(), hasItem(new ModelPathComponent(path("a.b.c"))));
			assertThat(registration.getActions(), emptyIterable());
			assertThat(registration.getComponents(), iterableWithSize(2)); // for projections
		});
	}

	@Test
	void canCreateRegistrationOfUnmanagedType() {
		val registration = NodeRegistration.unmanaged("z", of(MyType.class), alwaysThrow()).scope(path("x.y"));
		assertAll(() -> {
			assertThat(registration.getComponents(), hasItem(new ModelPathComponent(path("x.y.z"))));
			assertThat(registration.getActions(), emptyIterable());
			assertThat(registration.getComponents(), iterableWithSize(2)); // for projections
		});
	}

	@Test
	void whenNodeRegistrationAreScopedTheyAreEqualToAnEquivalentModelRegistration() {
		assertThat(NodeRegistration.of("a", of(MyType.class)).scope(path("foo")), equalTo(ModelRegistration.of("foo.a", MyType.class)));
		assertThat(NodeRegistration.of("b", of(MyType.class)).withComponent(ModelProjections.ofInstance("bar")).scope(path("bar")),
			equalTo(ModelRegistration.builder().withComponent(new ModelPathComponent(path("bar.b"))).withComponent(ModelProjections.managed(of(MyType.class))).withComponent(ModelProjections.ofInstance("bar")).build()));
	}

	@Test
	void canAddProjection() {
		val registration = NodeRegistration.of("c", of(MyType.class)).withComponent(ModelProjections.ofInstance("foo")).scope(path("ab"));
		assertAll(() -> {
			assertThat(registration.getComponents(), hasItem(new ModelPathComponent(path("ab.c"))));
			assertThat(registration.getActions(), emptyIterable());
			assertThat(registration.getComponents(), iterableWithSize(3)); // for projections
		});
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(NodeRegistration.of("a", of(MyType.class)), NodeRegistration.of("a", of(MyType.class)))
			.addEqualityGroup(NodeRegistration.of("c", of(MyType.class)))
			.addEqualityGroup(NodeRegistration.of("a", of(MyOtherType.class)))
			.addEqualityGroup(NodeRegistration.of("a", of(MyType.class)).withComponent(ModelProjections.ofInstance("foo")))
			.addEqualityGroup(NodeRegistration.of("a", of(MyType.class)).withComponent(ModelProjections.ofInstance("foo")).action(self(stateAtLeast(ModelState.Registered)).apply(doSomething())))
			.testEquals();
	}

	@Test
	void canAddActions() {
		val registration = NodeRegistration.of("bar", of(MyType.class)).action(self(stateAtLeast(ModelState.Registered)).apply(doSomething())).scope(path("foo"));
		assertThat(registration.getActions(), hasItem(matching(self(stateAtLeast(ModelState.Registered)).scope(path("foo.bar")), doSomething()))); // other are for projections
	}

	@Test
	void canAddActionsUsingNodePredicate() {
		val registration = NodeRegistration.of("b", of(MyType.class))
			.action(allDirectDescendants().apply(doSomething())).scope(path("a"));
		assertThat(registration.getActions(), hasItem(matching(allDirectDescendants().scope(path("a.b")), doSomething()))); // other are for projections
	}

	interface MyType {}
	interface MyOtherType {}
}
