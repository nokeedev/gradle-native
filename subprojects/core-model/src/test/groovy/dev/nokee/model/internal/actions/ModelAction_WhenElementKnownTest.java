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
package dev.nokee.model.internal.actions;

import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.state.ModelState;
import org.gradle.api.Action;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.nokee.model.internal.actions.ModelSpec.stateAtLeast;
import static dev.nokee.model.internal.actions.ModelSpec.subtypeOf;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

@ExtendWith(MockitoExtension.class)
class ModelAction_WhenElementKnownTest {
	@Nested
	class ByTypeOnlyTest implements ModelActionTester {
		@Mock private Action<KnownDomainObject<MyType>> action;
		private ModelRegistration subject;

		@BeforeEach
		void createSubject() {
			subject = ModelAction.whenElementKnown(MyType.class, action);
		}

		@Override
		public ModelRegistration subject() {
			return subject;
		}

		@Test
		void usesIntersectionOfSpecifiedSpec_Type_AndStateInComponent() {
			assertThat(subject.getComponents(), hasItem(new ModelSpecComponent(subtypeOf(of(MyType.class)).and(stateAtLeast(ModelState.Registered)))));
		}

		@Test
		void usesSpecifiedActionInComponent() {
			assertThat(subject.getComponents(), hasItem(new ModelActionComponent(new ModelActionKnownDomainObjectAdapter<>(of(MyType.class), action))));
		}
	}

	@Nested
	class BySpecAndTypeTest implements ModelActionTester {
		@Mock private ModelSpec spec;
		@Mock private Action<KnownDomainObject<MyType>> action;
		private ModelRegistration subject;

		@BeforeEach
		void createSubject() {
			subject = ModelAction.whenElementKnown(spec, MyType.class, action);
		}

		@Override
		public ModelRegistration subject() {
			return subject;
		}

		@Test
		void usesIntersectionOfSpecifiedSpecAndTypeAndStateInComponent() {
			assertThat(subject.getComponents(), hasItem(new ModelSpecComponent(subtypeOf(of(MyType.class)).and(stateAtLeast(ModelState.Registered)).and(spec))));
		}

		@Test
		void usesSpecifiedActionInComponent() {
			assertThat(subject.getComponents(), hasItem(new ModelActionComponent(new ModelActionKnownDomainObjectAdapter<>(of(MyType.class), action))));
		}
	}

	private interface MyType {}
}
