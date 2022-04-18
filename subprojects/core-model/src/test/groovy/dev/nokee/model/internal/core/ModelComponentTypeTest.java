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

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import dev.nokee.internal.Factories;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelComponentType.componentOf;
import static dev.nokee.model.internal.core.ModelComponentType.ofInstance;
import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelComponentTypeTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void noEqualityBetweenProjectionAndComponentOfSameType() {
		new EqualsTester()
			.addEqualityGroup(componentOf(MyComponent.class))
			.addEqualityGroup(projectionOf(MyType.class))
			.testEquals();
	}

	@Test
	void checkComponentTypeAgainstComponentInstance() {
		assertThat(ofInstance(new MyComponent()), equalTo(componentOf(MyComponent.class)));
	}

	@Test
	void canTestSuperProjectionType() {
		assertTrue(projectionOf(IMyType.class).isSupertypeOf(projectionOf(MyType.class)));
		assertFalse(projectionOf(MyType.class).isSupertypeOf(projectionOf(IMyType.class)));
	}

	@Test
	void projectionTypesCannotBeSuperTypeOfComponentTypes() {
		assertFalse(projectionOf(IMyType.class).isSupertypeOf(componentOf(MyComponent.class)));
		assertFalse(projectionOf(MyType.class).isSupertypeOf(componentOf(MyComponent.class)));
	}

	@Test
	void componentTypesCannotBeSuperTypeOfProjectionTypes() {
		assertFalse(componentOf(MyComponent.class).isSupertypeOf(projectionOf(MyType.class)));
		assertFalse(componentOf(MyComponent.class).isSupertypeOf(projectionOf(IMyType.class)));
	}

	public interface IMyType {}
	public static class MyType implements IMyType {}
	public static class MyComponent implements ModelComponent {}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkProjectionTypeEquality() {
		new EqualsTester()
			.addEqualityGroup(projectionOf(ProjectionA.class), projectionOf(ProjectionA.class))
			.addEqualityGroup(projectionOf(ProjectionB.class))
			.testEquals();
	}

	private interface ProjectionA {}
	private interface ProjectionB {}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkComponentTypeEquality() {
		new EqualsTester()
			.addEqualityGroup(componentOf(ComponentA.class), componentOf(ComponentA.class))
			.addEqualityGroup(componentOf(ComponentB.class))
			.testEquals();
	}

	private interface ComponentA extends ModelComponent {}
	private interface ComponentB extends ModelComponent {}

	@Test
	void checkProjectionTypeAgainstProjectionInstance() {
		assertAll(
			() -> assertThat(ofInstance(ModelProjections.ofInstance(new MyProjection())),
				equalTo(projectionOf(MyProjection.class))),
			() -> assertThat(ofInstance(ModelProjections.managed(of(MyProjection.class))),
				equalTo(projectionOf(MyProjection.class))),
			() -> assertThat(ofInstance(ModelProjections.managed(of(MyProjection.class))),
				equalTo(projectionOf(MyProjection.class))),
			() -> assertThat(ofInstance(ModelProjections.createdUsing(of(MyProjection.class), Factories.alwaysThrow())),
				equalTo(projectionOf(MyProjection.class)))
		);
	}

	public static class MyProjection {}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicStaticMethods(ModelComponentType.class);
	}
}
