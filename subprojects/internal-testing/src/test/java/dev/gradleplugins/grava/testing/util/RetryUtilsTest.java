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
package dev.gradleplugins.grava.testing.util;

import dev.nokee.internal.testing.util.RetryUtils;
import lombok.val;
import org.apache.commons.lang3.function.FailableRunnable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class RetryUtilsTest {
	@ParameterizedTest(name = "exceed retry count of {0} throws last exception")
	@ValueSource(ints = {1, 2, 3, 10})
    void exceedRetryCountThrowsLastException(int retryCount) throws Throwable {
		@SuppressWarnings("unchecked")
		val closure = (FailableRunnable<Throwable>) mock(FailableRunnable.class);
		for (int retryIndex = 0; retryIndex < retryCount; ++retryIndex) {
			doThrow(new RuntimeException("Exception for retry #" + retryIndex)).when(closure).run();
		}

        val ex = assertThrows(Exception.class, () -> RetryUtils.retry(retryCount, closure));
        assertThat(ex.getMessage(), equalTo("Exception for retry #" + (retryCount - 1)));
    }

    @Test
    void throwingExceptionWillCountAsAnTry() {
        assertThrows(Exception.class, () -> RetryUtils.retry(1, () -> { throw new Exception(); }));
    }

    @Test
    void closureSucceedOnSecondTryReturnsRetryCountOfTwo() throws Throwable {
		@SuppressWarnings("unchecked")
		val closure = (FailableRunnable<Throwable>) mock(FailableRunnable.class);
		doThrow(new RuntimeException()).doNothing().when(closure).run();
        assertThat(RetryUtils.retry(2, closure), is(2));
    }
}
