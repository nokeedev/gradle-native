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
package dev.nokee.utils;

import org.gradle.api.Action;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.nokee.internal.testing.util.ProjectTestUtils.providerFactory;
import static dev.nokee.utils.ProviderUtils.ifPresent;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProviderUtils_IfPresentTest {
	static final Object VALUE = new Object();
	@Mock Action<Object> action;

	@Test
	void invokesActionWhenProviderValueIsPresent() {
		ifPresent(providerFactory().provider(() -> VALUE), action);
		verify(action).execute(VALUE);
	}

	@Test
	void doesNotInvokeActionWhenProviderValueIsAbsent() {
		ifPresent(providerFactory().provider(() -> null), action);
		verify(action, never()).execute(any());
	}

	@Test
	void checkNulls() {
		assertAll(
			() -> assertThrows(NullPointerException.class, () -> ifPresent(null, action)),
			() -> assertThrows(NullPointerException.class, () -> ifPresent(providerFactory().provider(() -> VALUE), null)),
			() -> assertThrows(NullPointerException.class, () -> ifPresent(providerFactory().provider(() -> null), null))
		);
	}
}
