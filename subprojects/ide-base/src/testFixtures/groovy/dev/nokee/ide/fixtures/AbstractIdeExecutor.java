/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.ide.fixtures;

import dev.gradleplugins.runnerkit.BuildResult;
import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionFailure;
import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionResult;
import dev.gradleplugins.test.fixtures.gradle.executer.internal.ExecutionResultImpl;
import dev.nokee.core.exec.CommandLineToolExecutionResult;
import dev.nokee.core.exec.CommandLineToolInvocation;
import dev.nokee.core.exec.CommandLineToolProvider;
import dev.nokee.core.exec.ProcessBuilderEngine;
import lombok.Getter;
import lombok.Value;
import lombok.val;
import org.hamcrest.Matchers;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables.from;
import static dev.nokee.core.exec.CommandLineToolInvocationOutputRedirection.toSystemError;
import static dev.nokee.core.exec.CommandLineToolInvocationOutputRedirection.toSystemOutput;

public abstract class AbstractIdeExecutor<T extends AbstractIdeExecutor<T>> {
	private final Class<T> type;
	private final CommandLineToolProvider tool;
	private final List<String> arguments = new ArrayList<>();
	@Getter private TestFile workingDirectory;

	protected AbstractIdeExecutor(TestFile workingDirectory, Class<T> type, CommandLineToolProvider tool) {
		this.workingDirectory = workingDirectory;
		this.type = type;
		this.tool = tool;
	}

	protected abstract IdeAction getDefaultIdeAction();

	public T withWorkingDirectory(TestFile workingDirectory) {
		this.workingDirectory = workingDirectory;
		return type.cast(this);
	}

	public T addArguments(String... arguments) {
		this.arguments.addAll(Arrays.asList(arguments));
		return type.cast(this);
	}

	public T withArgument(String argument) {
		this.arguments.add(argument);
		return type.cast(this);
	}

	public T withArguments(String... arguments) {
		this.arguments.clear();
		this.arguments.addAll(Arrays.asList(arguments));
		return type.cast(this);
	}

	protected CommandLineToolExecutionResult doExecute() {
		return tool.get()
			.withArguments(arguments)
			.newInvocation()
			.redirectStandardOutput(toSystemOutput())
			.redirectErrorOutput(toSystemError())
			.workingDirectory(workingDirectory)
			.withEnvironmentVariables(from(IdeCommandLineUtils.buildEnvironment(workingDirectory)))
			.buildAndSubmit(new ProcessBuilderEngine())
			.waitFor();
	}

	protected CommandLineToolInvocation.Builder configureInvocation(CommandLineToolInvocation.Builder builder) {
		return builder;
	}

	public CommandLineToolExecutionResult execute() {
		withArgument(asArgument(getDefaultIdeAction()));
		return doExecute();
	}

	public ExecutionResult run() {
		return run(getDefaultIdeAction());
	}

	public ExecutionResult run(IdeAction action) {
		withArgument(asArgument(action));
		val result = doExecute();
		if (result.getExitValue() == 0) {
			return asExecutionResult(result);
		}
		return asExecutionFailure(result);
	}

	public ExecutionResult succeeds() {
		return succeeds(getDefaultIdeAction());
	}

	public ExecutionResult succeeds(IdeAction action) {
		withArgument(asArgument(action));
		val result = doExecute().assertNormalExitValue();
		return asExecutionResult(result);
	}

	protected ExecutionResult asExecutionResult(CommandLineToolExecutionResult result) {
		return new ExecutionResultImpl(BuildResult.from(result.getOutput().getAsString()));
	}

	public ExecutionFailure fails() {
		return fails(getDefaultIdeAction());
	}

	public ExecutionFailure fails(IdeAction action) {
		withArgument(asArgument(action));
		val result = doExecute();
		Assert.assertThat(result.getExitValue(), Matchers.not(Matchers.equalTo(0)));
		return asExecutionFailure(result);
	}

	protected ExecutionFailure asExecutionFailure(CommandLineToolExecutionResult result) {
		return new ExecutionResultImpl(BuildResult.from(result.getOutput().getAsString()));
	}

	protected String asArgument(IdeAction action) {
		return action.getIdentifier();
	}

	@Value(staticConstructor = "of")
	public static class IdeAction {
		@Getter String identifier;
	}
}
