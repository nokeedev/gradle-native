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
package dev.gradleplugins.exemplarkit;

import dev.nokee.core.exec.CommandLine;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.LoggingEngine;
import dev.nokee.core.exec.ProcessBuilderEngine;
import lombok.val;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;

import static dev.gradleplugins.exemplarkit.StepExecutionResult.*;
import static dev.nokee.core.exec.CommandLineToolInvocationErrorOutputRedirect.duplicateToSystemError;
import static dev.nokee.core.exec.CommandLineToolInvocationStandardOutputRedirect.duplicateToSystemOutput;
import static org.apache.commons.io.FilenameUtils.separatorsToSystem;

public final class StepExecutors {
	public static StepExecutor skipIf(Predicate<Step> executionCondition) {
		return new StepExecutor() {
			@Override
			public boolean canHandle(Step step) {
				return executionCondition.test(step);
			}

			@Override
			public StepExecutionResult run(StepExecutionContext context) {
				return stepSkipped();
			}
		};
	}

	public static StepExecutor replaceIfAbsent(String original, String target) {
		return new StepExecutor() {
			@Override
			public boolean canHandle(Step step) {
				return step.getExecutable().equals(original) && !CommandLineTool.fromPath(original).isPresent();
			}

			@Override
			public StepExecutionResult run(StepExecutionContext context) {
				val result = CommandLine.script(target, context.getCurrentStep().getArguments())
					.newInvocation()
					.workingDirectory(context.getCurrentWorkingDirectory())
					.redirectStandardOutput(duplicateToSystemOutput())
					.redirectErrorOutput(duplicateToSystemError())
					.buildAndSubmit(LoggingEngine.wrap(new ProcessBuilderEngine()))
					.waitFor();

				return stepExecuted(result.getExitValue(), result.getOutput().getAsString());
			}
		};
	}

	/**
	 * Supports executing {@literal cd} command when running an exemplar.
	 *
	 * @return a step executor handling {@literal cd} command, never null
	 */
	public static StepExecutor changeDirectory() {
		return ChangeDirectoryExecutor.INSTANCE;
	}

	private enum ChangeDirectoryExecutor implements StepExecutor {
		INSTANCE;

		@Override
		public boolean canHandle(Step step) {
			// Handle `cd <path>`
			return step.getExecutable().equals("cd") && step.getArguments().size() == 1;
		}

		@Override
		public StepExecutionResult run(StepExecutionContext context) {
			try {

				val newCurrentWorkingDirectory = computeNewCurrentWorkingDirectory(context).toFile().getCanonicalFile();
				if (!context.getSandboxDirectory().getCanonicalFile().equals(newCurrentWorkingDirectory) && context.getSandboxDirectory().getCanonicalPath().startsWith(newCurrentWorkingDirectory.getAbsolutePath())) {
					throw new IllegalStateException("Cannot change directory outside of sandbox directory.");
				}
				if (!newCurrentWorkingDirectory.exists()) {
					return stepExecuted(1);
				}
				context.setCurrentWorkingDirectory(newCurrentWorkingDirectory);
			} catch (IOException e) {
				return stepFailed(e);
			}

			return stepExecuted(0);
		}

		private Path computeNewCurrentWorkingDirectory(StepExecutionContext context) {
			val result = Paths.get(context.getCurrentStep().getArguments().get(0));
			if (result.isAbsolute()) {
				return result;
			}
			return context.getCurrentWorkingDirectory().toPath().resolve(result);
		}
	}

	public static StepExecutor hostTerminal() {
		return DefaultStepExecutor.INSTANCE;
	}

	private enum DefaultStepExecutor implements StepExecutor {
		INSTANCE;

		@Override
		public boolean canHandle(Step step) {
			return true;
		}

		private static CommandLine commandLine(StepExecutionContext context) {
			String executable = context.getCurrentStep().getExecutable();
			if (executable.startsWith("./") && SystemUtils.IS_OS_WINDOWS) {
				executable = separatorsToSystem(executable.substring(2));
			}
			return CommandLine.script(executable, context.getCurrentStep().getArguments());
		}

		@Override
		public StepExecutionResult run(StepExecutionContext context) {
			val result = commandLine(context)
				.newInvocation()
				.redirectStandardOutput(duplicateToSystemOutput())
				.redirectErrorOutput(duplicateToSystemError())
				.workingDirectory(context.getCurrentWorkingDirectory())
				.buildAndSubmit(LoggingEngine.wrap(new ProcessBuilderEngine()))
				.waitFor();

			String output = result.getOutput().withNormalizedEndOfLine().getAsString();
			if (output.isEmpty()) {
				return stepExecuted(result.getExitValue());
			}
			return stepExecuted(result.getExitValue(), output);
		}
	}
}
