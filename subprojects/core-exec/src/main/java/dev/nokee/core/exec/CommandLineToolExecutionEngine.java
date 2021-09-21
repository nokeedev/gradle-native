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

/**
 * A execution engine models how a command line tool will be executed.
 * It convert an agnostic invocation into an handle for the execution within the engine.
 *
 * @param <T> The handle type returned by this engine for each submitted invocation.
 */
public interface CommandLineToolExecutionEngine<T extends CommandLineToolExecutionHandle> {
	/**
	 * Schedule specified {@link CommandLineToolInvocation} for execution using this engine.
	 * Once the execution is scheduled, it's up to the engine to decide when the execution will start.
	 * The returned handle allows some control over the execution, not all engine support the same set of features.
	 * Refer to the specific execution engine documentation.
	 *
	 * <p>
	 *     NOTE: A execution engine may not support all the feature an invocation may be requesting.
	 *     It is up to the strategy of each engine to decide if the feature will be ignored or an exception will be thrown.
	 * </p>
	 *
	 * @param invocation the command line invocation information to execute
	 * @return a {@link CommandLineToolExecutionHandle} instance, never null.
	 */
	T submit(CommandLineToolInvocation invocation);
}
