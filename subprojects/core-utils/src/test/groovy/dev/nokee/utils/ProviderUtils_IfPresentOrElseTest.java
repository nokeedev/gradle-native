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
import static dev.nokee.utils.ProviderUtils.ifPresentOrElse;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ProviderUtils_IfPresentOrElseTest {
	static final Object VALUE = new Object();
	@Mock Action<Object> presentAction;
	@Mock Runnable absentAction;

	@Test
	void executesActionWithValueIfPresent() {
		ifPresentOrElse(providerFactory().provider(() -> VALUE), presentAction, absentAction);
		Mockito.verify(presentAction).execute(VALUE);
		Mockito.verifyNoInteractions(absentAction);
	}

	@Test
	void executesEmptyActionIfAbsent() {
		ifPresentOrElse(providerFactory().provider(() -> null), presentAction, absentAction);
		Mockito.verifyNoInteractions(presentAction);
		Mockito.verify(absentAction).run();
	}

	@Test
	void checkNulls() {
		assertAll(
			() -> assertThrows(NullPointerException.class, () -> ifPresentOrElse(null, presentAction, absentAction)),
			() -> assertThrows(NullPointerException.class, () -> ifPresentOrElse(providerFactory().provider(() -> null), null, absentAction)),
			() -> assertThrows(NullPointerException.class, () -> ifPresentOrElse(providerFactory().provider(() -> VALUE), presentAction, null))
		);
	}
}
