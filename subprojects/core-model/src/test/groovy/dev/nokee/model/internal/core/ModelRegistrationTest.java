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
import static dev.nokee.model.internal.core.ModelRegistration.bridgedInstance;
import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.core.ModelRegistration.unmanagedInstance;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.jupiter.api.Assertions.assertAll;

class ModelRegistrationTest {
	@Test
	void canCreateFromRawPathAndRawType() {
		assertAll(() -> {
			val registration = ModelRegistration.of("a.b.c", MyType.class);
			assertThat(registration.getComponents(), hasItem(new ModelPathComponent(path("a.b.c"))));
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
				builder().withComponent(new ModelPathComponent(path("po.ta.to"))).withComponent(ModelProjections.managed(of(MyOtherType.class))).build())
			.addEqualityGroup(
				builder().withComponent(new ModelPathComponent(path("po.ta.to"))).build())
			.addEqualityGroup(
				builder().withComponent(new ModelPathComponent(path("po.ta.to"))).withComponent(ModelProjections.ofInstance(new MyType())).build())
			.testEquals();
	}

	@Test
	void canCreateRegistrationForInstance() {
		assertAll(() -> {
			val registration = unmanagedInstance(of("foo", MyType.class), alwaysThrow());
			assertThat(registration.getComponents(), hasItem(new ModelPathComponent(path("foo"))));
			assertThat(registration.getComponents(), iterableWithSize(2)); // for the projections
		});
	}

	@Test
	void canCreateRegistrationForDeferredInstance() {
		assertAll(() -> {
			val registration = bridgedInstance(of("foo", MyType.class), new MyType());
			assertThat(registration.getComponents(), hasItem(new ModelPathComponent(path("foo"))));
			assertThat(registration.getComponents(), iterableWithSize(2)); // for the projections
		});
	}

	static class MyType {}
	static class MyOtherType {}
}
