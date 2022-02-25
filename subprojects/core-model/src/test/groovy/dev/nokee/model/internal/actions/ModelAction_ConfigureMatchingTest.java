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

import dev.nokee.model.internal.core.ModelRegistration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

@ExtendWith(MockitoExtension.class)
class ModelAction_ConfigureMatchingTest implements ModelActionTester {
	@Mock private ModelSpec spec;
	@Mock private ModelAction action;
	private ModelRegistration subject;

	@BeforeEach
	void createSubject() {
		subject = ModelAction.configureMatching(spec, action);
	}

	@Override
	public ModelRegistration subject() {
		return subject;
	}

	@Test
	void usesSpecifiedSpecInComponent() {
		assertThat(subject.getComponents(), hasItem(new ModelSpecComponent(spec)));
	}

	@Test
	void usesSpecifiedActionInComponent() {
		assertThat(subject.getComponents(), hasItem(new ModelActionComponent(action)));
	}
}
