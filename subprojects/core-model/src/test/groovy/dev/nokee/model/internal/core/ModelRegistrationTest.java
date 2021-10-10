/*
 * Copyright 2020 the original author or authors.
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
import com.google.common.testing.NullPointerTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.Factories.alwaysThrow;
import static dev.nokee.model.internal.core.ModelIdentifier.of;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelRegistration.*;
import static dev.nokee.model.internal.core.ModelTestActions.doSomething;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ModelRegistrationTest {
	@Test
	void canCreateFromRawPathAndRawType() {
		assertAll(() -> {
			val registration = ModelRegistration.of("a.b.c", MyType.class);
			assertEquals(path("a.b.c"), registration.getPath());
			assertEquals(of(MyType.class), registration.getDefaultProjectionType());
			assertThat(registration.getActions(), emptyIterable());
			assertThat(registration.getComponents(), iterableWithSize(2)); // for the projections
		});
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().setDefault(ModelIdentifier.class, of("x.y.z", MyType.class)).testAllPublicStaticMethods(ModelRegistration.class);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(
				ModelRegistration.of("po.ta.to", MyType.class),
				ModelRegistration.of("po.ta.to", MyType.class))
			.addEqualityGroup(ModelRegistration.of("to.ma.to", MyType.class))
			.addEqualityGroup(
				ModelRegistration.of("po.ta.to", MyOtherType.class),
				builder().withPath(path("po.ta.to")).withComponent(ModelProjections.managed(of(MyOtherType.class))).build())
			.addEqualityGroup(
				builder().withPath(path("po.ta.to")).build(),
				builder().withPath(path("po.ta.to")).withDefaultProjectionType(of(MyType.class)).build())
			.addEqualityGroup(
				builder().withPath(path("po.ta.to")).withComponent(ModelProjections.ofInstance(new MyType())).build())
			.addEqualityGroup(
				builder().withPath(path("po.ta.to")).action(doSomething()).build())
			.testEquals();
	}

	@Test
	void canCreateRegistrationForInstance() {
		assertAll(() -> {
			val registration = unmanagedInstance(of("foo", MyType.class), alwaysThrow());
			assertEquals(path("foo"), registration.getPath());
			assertEquals(of(MyType.class), registration.getDefaultProjectionType());
			assertThat(registration.getActions(), emptyIterable());
			assertThat(registration.getComponents(), iterableWithSize(2)); // for the projections
		});
	}

	@Test
	void canCreateRegistrationForDeferredInstance() {
		assertAll(() -> {
			val registration = bridgedInstance(of("foo", MyType.class), new MyType());
			assertEquals(path("foo"), registration.getPath());
			assertEquals(of(MyType.class), registration.getDefaultProjectionType());
			assertThat(registration.getActions(), emptyIterable());
			assertThat(registration.getComponents(), iterableWithSize(2)); // for the projections
		});
	}

	@Test
	void canAddActionToRegistration() {
		assertAll(() -> {
			val registration = ModelRegistration.builder()
				.withPath(path("a.b.c"))
				.withDefaultProjectionType(of(MyType.class))
				.action(doSomething())
				.build();
			assertThat(registration.getActions(), hasItem(doSomething())); // other are projections
		});
	}

	static class MyType {}
	static class MyOtherType {}
}
