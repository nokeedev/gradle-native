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
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.registry.ModelNodeBackedKnownDomainObject;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.internal.Cast;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelActions.executeAsKnownProjection;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static dev.nokee.model.internal.core.ModelTestUtils.projectionOf;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.ActionUtils.doNothing;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ModelActions_ExecuteAsKnownProjectionTest {
	@Test
	void checkToString() {
		assertThat(executeAsKnownProjection(of(MyType.class), doNothing()), hasToString("ModelActions.executeAsKnownProjection(interface dev.nokee.model.internal.core.ModelActions_ExecuteAsKnownProjectionTest$MyType, ActionUtils.doNothing())"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(executeAsKnownProjection(of(MyType.class), doNothing()), executeAsKnownProjection(of(MyType.class), doNothing()))
			.addEqualityGroup(executeAsKnownProjection(of(WrongType.class), doNothing()))
			.addEqualityGroup(executeAsKnownProjection(of(MyType.class), it -> { /* something */ }))
			.testEquals();
	}

	@Test
	void executeActionWhenProjectionIsKnown() {
		Action<KnownDomainObject<MyType>> action = Cast.uncheckedCast(mock(Action.class));
		val node = node(projectionOf(MyType.class));
		assertDoesNotThrow(() -> executeAsKnownProjection(of(MyType.class), action).execute(node));
		verify(action, times(1)).execute(new ModelNodeBackedKnownDomainObject<>(of(MyType.class), node));
	}

	@Test
	void throwExceptionWhenProjectionIsUnknown() {
		Action<KnownDomainObject<WrongType>> action = Cast.uncheckedCast(mock(Action.class));
		assertThrows(IllegalArgumentException.class, () -> executeAsKnownProjection(of(WrongType.class), action).execute(node(projectionOf(MyType.class))));
		verify(action, never()).execute(any());
	}

	interface MyType {}
	interface WrongType {}
}
