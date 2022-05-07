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

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.notNullValue;

public interface ModelRegistrationTester {
	ModelRegistration subject();

	@Test
	default void hasComponents() {
		assertThat(subject().getComponents(), notNullValue(List.class));
	}

	@Test
	default void returnsAnImmutableComponentList() {
		val initialSize = subject().getComponents().size();
		try {
			subject().getComponents().add(new Object());
		} catch (Throwable ignored) {
			// Components can either be a list copy or an immutable list
		}
		assertThat(subject().getComponents(), iterableWithSize(initialSize));
	}
}
