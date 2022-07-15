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

import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.Callable;

import static dev.nokee.internal.testing.util.ProjectTestUtils.providerFactory;
import static dev.nokee.utils.ProviderUtils.resolve;

@ExtendWith(MockitoExtension.class)
class ProviderUtils_ResolveTest {
	@Mock Callable<Object> callable;
	Provider<Object> subject;

	@BeforeEach
	void setUp() {
		subject = providerFactory().provider(callable);
	}

	@Test
	void callsSourceProvider() throws Exception {
		resolve(subject);
		Mockito.verify(callable).call();
	}
}
