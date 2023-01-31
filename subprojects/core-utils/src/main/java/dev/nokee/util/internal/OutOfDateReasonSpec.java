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
package dev.nokee.util.internal;

import lombok.EqualsAndHashCode;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.specs.Spec;

public final class OutOfDateReasonSpec<T extends Task> implements Spec<Task> {
	private static final Logger LOGGER = Logging.getLogger(OutOfDateReasonSpec.class);
	private final String outOfDateReason;
	private final Spec<? super T> delegate;
	private final InformationLogger logger;

	public OutOfDateReasonSpec(String outOfDateReason, Spec<? super T> delegate) {
		this(outOfDateReason, delegate, new DefaultInformationLogger());
	}

	// visible for testing
	public OutOfDateReasonSpec(String outOfDateReason, Spec<? super T> delegate, InformationLogger logger) {
		this.outOfDateReason = outOfDateReason;
		this.delegate = delegate;
		this.logger = logger;
	}

	@EqualsAndHashCode
	private static final class DefaultInformationLogger implements InformationLogger {
//		private final Logger logger;
//
//		private DefaultInformationLogger(Logger logger) {
//			this.logger = logger;
//		}

		@Override
		public void log(String message) {
			LOGGER.info(message);
		}
	}

	// Returning {@literal true} means can be up-to-date while returning {@literal false} means out-of-date.
	@Override
	public boolean isSatisfiedBy(Task task) {
		assert task != null : "'task' must not be null";
		@SuppressWarnings("unchecked")
		T castedTask = (T) task;
		boolean result = delegate.isSatisfiedBy(castedTask);
		if (isOutOfDate(result)) {
			logger.log(String.format("Task outputs are out-of-date because %s.", outOfDateReason));
		}
		return result;
	}

	private static boolean isOutOfDate(boolean specResult) {
		return !specResult;
	}

	public static <TaskType extends Task> Spec<Task> because(String outOfDateReason, Spec<? super TaskType> spec) {
		return new OutOfDateReasonSpec<>(outOfDateReason, spec);
	}

	public interface InformationLogger {
		void log(String message);
	}
}
