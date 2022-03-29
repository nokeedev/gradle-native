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

import java.io.File;
import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * A build experiment runner executes individual performance scenarios.
 * The scenarios are then aggregated to express a performance experiment.
 *
 * The scenarios are isolated execution that are part of a bigger experiment.
 * It can compare the same build across multiple version of the same tool.
 * Or it can compare similar build across multiple tools.
 */
public interface BuildExperimentRunner<I extends BuildExperimentInvocationSpec> {
	static <I extends BuildExperimentInvocationSpec> BuildExperimentRunner<I> create(BuildExperimentExecutor<I> executor) {
		return new BuildExperimentRunnerImpl<>(executor);
	}

	/**
	 * Sets the working directory to use.
	 *
	 * @param workingDirectory the working directory to use, must not be null
	 * @return a new {@link BuildExperimentRunner} instance configured with the specified working directory, never null
	 */
	BuildExperimentRunner<I> inDirectory(File workingDirectory);

	/**
	 * Sets the working directory to use.
	 *
	 * @param workingDirectory the working directory to use, must not be null
	 * @return a new {@link BuildExperimentRunner} instance configured with the specified working directory, never null
	 */
	BuildExperimentRunner<I> inDirectory(Path workingDirectory);

	/**
	 * Sets the working directory to use using the supplier.
	 *
	 * @param workingDirectorySupplier a working directory supplier to use, must not be null
	 * @return a new {@link BuildExperimentRunner} instance configured with the specified working directory, never null
	 */
	BuildExperimentRunner<I> inDirectory(Supplier<?> workingDirectorySupplier);

	/**
	 * The directory that the build will be executed in.
	 * <p>
	 * This is analogous to the current directory when executing Gradle from the command line.
	 *
	 * @return the directory to execute the build in
	 * @throws InvalidRunnerConfigurationException if the working directory is not configured
	 */
	File getWorkingDirectory() throws InvalidRunnerConfigurationException;


	/**
	 * The number of runs to perform prior to the start of the build experiment.
	 * When the experiment doesn't require any warm-ups, you can safely set the count to zero.
	 *
	 * @param count  the number of warm-up runs to perform, must not be negative
	 * @return a new {@link BuildExperimentRunner} instance configured with the specified warm-up runs count, never null
	 */
	BuildExperimentRunner<I> withWarmUpRuns(int count);

	/**
	 * The number of runs to perform as part of the build experiment.
	 * The ideal number of invocation depend on the executor and experiment.
	 *
	 * @param count  the number of invocation runs to perform, must not be negative
	 * @return a new {@link BuildExperimentRunner} instance configured with the specified invocation runs count, never null
	 */
	BuildExperimentRunner<I> withInvocationRuns(int count);

	/**
	 * The build action to perform as part of the build experiment.
	 * The available action differ per executor.
	 * A build action must be provided for a build experiment to complete successfully.
	 * If the build action is expected to cause a failure, you need to mark the runner as such using {@link #expectFailure()}.
	 *
	 * @param buildAction  the build action to execute on each warm-up and invocation runs, must not be null
	 * @return a new {@link BuildExperimentRunner} instance configured with the specified build action, never null
	 */
	BuildExperimentRunner<I> withBuildAction(BuildExperimentAction buildAction);

	/**
	 * The optional clean action to perform as part of the build experiment.
	 * The available action differ per executor.
	 * If a clean action is provided, it will always be executed before the build action.
	 * The clean action is not expected to fail.
	 *
	 * @param cleanAction  the clean action to execute on each warm-up and invocation runs, must not be null
	 * @return a new {@link BuildExperimentRunner} instance configured with the specified clean action, never null
	 */
	BuildExperimentRunner<I> withCleanAction(BuildExperimentAction cleanAction);

	// TODO: General "build mutator"
	//   Mutator simply change the build according something specific (ABI change vs non-ABI change or full clean vs incremental).

	BuildExperimentRunner<I> expectFailure();

	BuildExperimentRunner<I> invocation(UnaryOperator<I> action);

	BuildExperimentResult run();
}
