/*
 * Copyright 2017 the original author or authors.
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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.function.FailableRunnable;

import java.time.Duration;

public final class RetryUtils {
    private RetryUtils() {}

    public static <E extends Throwable> int retry(FailableRunnable<E> closure) throws InterruptedException, E {
        return retry(3, closure);
    }

    public static <E extends Throwable> int retry(int retries, FailableRunnable<E> closure) throws InterruptedException, E {
        return retry(retries, Duration.ZERO, closure);
    }

    public static <E extends Throwable> int retry(int retries, Duration waitBetweenRetries, FailableRunnable<E> closure) throws InterruptedException, E {
        int retryCount = 0;
        Throwable lastException = null;

        while (retryCount++ < retries) {
            try {
                closure.run();
                return retryCount;
            } catch (Throwable e) {
                lastException = e;
                Thread.sleep(waitBetweenRetries.toMillis());
            }
        }

        // Retry count exceeded, throwing last exception
        return ExceptionUtils.rethrow(lastException);
    }
}
