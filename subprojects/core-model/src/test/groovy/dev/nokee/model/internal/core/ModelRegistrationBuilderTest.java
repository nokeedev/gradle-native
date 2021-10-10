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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.notNullValue;

class ModelRegistrationBuilderTest {
	private final ModelRegistration.Builder<?> subject = builder();

	@Test
	void canCreateBuilder() {
		assertThat(subject, notNullValue());
	}

	@Nested
	class DefaultBuildTest implements ModelRegistrationTester {
		private final ModelRegistration<?> subject = ModelRegistrationBuilderTest.this.subject.build();

		@Override
		public ModelRegistration<?> subject() {
			return subject;
		}

		@Test
		void hasNoActions() {
			assertThat(subject().getActions(), emptyIterable());
		}
	}
}
