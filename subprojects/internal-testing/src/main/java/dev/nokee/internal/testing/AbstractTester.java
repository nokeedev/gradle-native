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
package dev.nokee.internal.testing;

import dev.nokee.internal.testing.TestCase;
import lombok.val;
import org.opentest4j.MultipleFailuresError;
import org.opentest4j.TestAbortedException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public abstract class AbstractTester {
	protected final List<TestCase> getTesters() {
		val result = new ArrayList<TestCase>();
		collectTesters(result);
		return result;
	}

	protected abstract void collectTesters(List<TestCase> testers);

	protected final void executeAllTestCases() {
		val failures = new ArrayList<TestCaseFailure>();
		stream().forEach(testCase -> {
			try {
				testCase.setUp();
				try {
					testCase.execute();
				} finally {
					testCase.tearDown();
				}
			} catch (TestAbortedException ex) {
				// ignore test
			} catch (Throwable throwable) {
				failures.add(new TestCaseFailure(testCase.getDisplayName(), throwable));
			}
		});
		if (!failures.isEmpty()) {
			throw new MultipleFailuresError("Plugin is not well-behaved", failures);
		}
	}

	private static final class TestCaseFailure extends RuntimeException {
		public TestCaseFailure(String displayName, Throwable throwable) {
			super(displayName, throwable);
		}
	}

	public final Stream<TestCase> stream() {
		return getTesters().stream();
	}
}
