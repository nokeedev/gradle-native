/*
 * Copyright 2020 the original author or authors.
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

import groovy.lang.Closure;
import lombok.Getter;
import lombok.val;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.junit.platform.commons.util.ExceptionUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ClosureAssertions {
	private final ClosureExecutionResult result;

	private ClosureAssertions(ClosureExecutionResult result) {
		this.result = result;
	}

	public static ClosureAssertions executeWith(ThrowingConsumer<Closure<Void>> execution) {
		val context = new ClosureExecutionContext();
		val closure = new TestClosure(context);
		try {
			execution.accept(closure);
		} catch (Throwable throwable) {
			ExceptionUtils.throwAsUncheckedException(throwable);
		}
		return new ClosureAssertions(context);
	}

	public interface ClosureExecutionResult {
		int getCalledCount();
		Object getLastCalledArgument();
		int getResolveStrategy();
	}

	private static class ClosureExecutionContext implements ClosureExecutionResult {
		@Getter int calledCount = 0;
		@Getter Object lastCalledArgument = null;
		@Getter int resolveStrategy;
	}

	public static class TestClosure extends Closure<Void> {
		private static final Object NULL_OWNER = new Object();
		private final ClosureExecutionContext context;

		public TestClosure(ClosureExecutionContext context) {
			super(NULL_OWNER);
			this.context = context;
		}

		public void doCall(Object t) {
			context.calledCount++;
			context.lastCalledArgument = t;
			context.resolveStrategy = getResolveStrategy();
		}
	}

	public ClosureAssertions assertCalledOnce() {
		assertEquals(1, result.getCalledCount(), "not called once");
		return this;
	}

	public ClosureAssertions assertDelegateFirstResolveStrategy() {
		assertEquals(Closure.DELEGATE_FIRST, result.getResolveStrategy(), "not a delegate first strategy");
		return this;
	}

	public ClosureAssertions assertLastCalledArgumentThat(Matcher<Object> matcher) {
		assertThat(result.getLastCalledArgument(), matcher);
		return this;
	}
}
