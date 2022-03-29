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
package dev.nokee.platform.cpp;

import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException;
import dev.gradleplugins.runnerkit.providers.BuildActionProvider;
import dev.gradleplugins.runnerkit.providers.CleanActionProvider;
import dev.gradleplugins.runnerkit.providers.ExpectedBuildResultProvider;
import dev.gradleplugins.runnerkit.providers.MeasurementRunsProvider;
import dev.gradleplugins.runnerkit.providers.InvocationSpecProvider;
import dev.gradleplugins.runnerkit.providers.WarmUpRunsProvider;
import dev.gradleplugins.runnerkit.providers.WorkingDirectoryProvider;
import lombok.val;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static java.util.Objects.requireNonNull;

final class BuildExperimentRunnerImpl<I extends BuildExperimentInvocationSpec> implements BuildExperimentRunner<I> {
	private final BuildExperimentExecutor<I> executor;
	private final BuildExperimentRunnerParameters<I> parameters;

	public BuildExperimentRunnerImpl(BuildExperimentExecutor<I> executor) {
		this(executor, new BuildExperimentRunnerParameters<I>(executor.getClass()));
	}

	private BuildExperimentRunnerImpl(BuildExperimentExecutor<I> executor, BuildExperimentRunnerParameters<I> parameters) {
		this.executor = executor;
		this.parameters = parameters;
	}

	private BuildExperimentRunnerImpl<I> newInstance(BuildExperimentRunnerParameters<I> parameters) {
		return new BuildExperimentRunnerImpl<>(executor, parameters);
	}

	@Override
	public BuildExperimentRunnerImpl<I> withWarmUpRuns(int count) {
		return newInstance(parameters.withWarmUpRuns(WarmUpRunsProvider.count(count)));
	}

	@Override
	public BuildExperimentRunnerImpl<I> withInvocationRuns(int count) {
		return newInstance(parameters.withMeasurementRuns(MeasurementRunsProvider.count(count)));
	}

	//region Working directory configuration
	@Override
	public BuildExperimentRunnerImpl<I> inDirectory(File workingDirectory) {
		return newInstance(parameters.withWorkingDirectory(WorkingDirectoryProvider.of(workingDirectory)));
	}

	@Override
	public BuildExperimentRunnerImpl<I> inDirectory(Path workingDirectory) {
		return newInstance(parameters.withWorkingDirectory(WorkingDirectoryProvider.of(workingDirectory.toFile())));
	}

	@Override
	public BuildExperimentRunnerImpl<I> inDirectory(Supplier<?> workingDirectorySupplier) {
		return newInstance(parameters.withWorkingDirectory(WorkingDirectoryProvider.of(() -> {
			val workingDirectory = requireNonNull(workingDirectorySupplier.get());
			if (workingDirectory instanceof File) {
				return (File) workingDirectory;
			} else if (workingDirectory instanceof Path) {
				return ((Path) workingDirectory).toFile();
			}
			throw new IllegalArgumentException(String.format("Supplied working directory cannot be converted to a File instance: %s", workingDirectory.getClass()));
		})));
	}

	@Override
	public File getWorkingDirectory() {
		if (parameters.getWorkingDirectory().isPresent()) {
			return parameters.getWorkingDirectory().get();
		}
		throw new InvalidRunnerConfigurationException("Please use GradleBuildExperimentRunner#inDirectory(File) API to configure a working directory for this runner.");
	}
	//endregion


	@Override
	public BuildExperimentRunnerImpl<I> withBuildAction(BuildExperimentAction buildAction) {
		return newInstance(parameters.withBuildAction(BuildActionProvider.of(buildAction)));
	}

	@Override
	public BuildExperimentRunnerImpl<I> withCleanAction(BuildExperimentAction cleanAction) {
		return newInstance(parameters.withCleanAction(CleanActionProvider.of(cleanAction)));
	}

	@Override
	public BuildExperimentRunner<I> expectFailure() {
		return newInstance(parameters.withExpectedBuildResult(ExpectedBuildResultProvider.failure()));
	}

	@Override
	public BuildExperimentRunner<I> invocation(UnaryOperator<I> action) {
		return newInstance(parameters.withInvocation(InvocationSpecProvider.of(action.apply(parameters.getInvocation().get()))));
	}

	@Override
	public BuildExperimentResult run() {
		val result = executor.run(parameters);
		return new DefaultBuildExperimentResult(result, parameters.getBuildAction().get(), parameters.getCleanAction().orElse(null));
	}
}
