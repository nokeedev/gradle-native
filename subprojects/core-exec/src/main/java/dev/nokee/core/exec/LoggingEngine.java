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
package dev.nokee.core.exec;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.logging.Logger;

public class LoggingEngine<T extends CommandLineToolExecutionHandle> implements CommandLineToolExecutionEngine<T> {
	private static final Logger LOGGER = Logger.getLogger(LoggingEngine.class.getName());
	private final CommandLineToolExecutionEngine<T> delegate;

	private LoggingEngine(CommandLineToolExecutionEngine<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public T submit(CommandLineToolInvocation invocation) {
		LOGGER.info(() -> String.format("Invoking process 'command '%s''. Command: %s", invocation.getExecutable().getLocation(), String.join(" ", Iterables.concat(ImmutableList.of(invocation.getExecutable().getLocation().toString()), invocation.getArguments().get()))));
		return delegate.submit(invocation);
	}

	public static <T extends CommandLineToolExecutionHandle> LoggingEngine<T> wrap(CommandLineToolExecutionEngine<T> engine) {
		return new LoggingEngine<>(engine);
	}
}
