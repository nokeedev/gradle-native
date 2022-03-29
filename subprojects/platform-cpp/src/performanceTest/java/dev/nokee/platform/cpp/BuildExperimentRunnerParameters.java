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

import dev.gradleplugins.runnerkit.providers.BuildActionProvider;
import dev.gradleplugins.runnerkit.providers.CleanActionProvider;
import dev.gradleplugins.runnerkit.providers.ExpectedBuildResultProvider;
import dev.gradleplugins.runnerkit.providers.MeasurementRunsProvider;
import dev.gradleplugins.runnerkit.providers.InvocationSpecProvider;
import dev.gradleplugins.runnerkit.providers.WarmUpRunsProvider;
import dev.gradleplugins.runnerkit.providers.WorkingDirectoryProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;

@Data
@AllArgsConstructor
final class BuildExperimentRunnerParameters<I extends BuildExperimentInvocationSpec> implements BuildExperimentExecutionContext<I> {
	private final Class<? extends BuildExperimentExecutor<I>> executorType;

	@With @NonNull private WarmUpRunsProvider warmUpRuns = WarmUpRunsProvider.count(5);
	@With @NonNull private MeasurementRunsProvider measurementRuns = MeasurementRunsProvider.count(5);

	@With @NonNull private WorkingDirectoryProvider workingDirectory = WorkingDirectoryProvider.unset();
	@With @NonNull private BuildActionProvider buildAction = BuildActionProvider.unset();
	@With @NonNull private CleanActionProvider cleanAction = CleanActionProvider.unset();
	@With @NonNull private ExpectedBuildResultProvider expectedBuildResult = ExpectedBuildResultProvider.success();
	@With @NonNull private InvocationSpecProvider<I> invocation;

	@SuppressWarnings({"unchecked"})
	BuildExperimentRunnerParameters(@SuppressWarnings("rawtypes") Class<? extends BuildExperimentExecutor> executorType) {
		this.executorType = (Class<? extends BuildExperimentExecutor<I>>) executorType;
		try {
			this.invocation = InvocationSpecProvider.of(((Class<I>) ((ParameterizedType) executorType.getGenericInterfaces()[0]).getActualTypeArguments()[0]).getDeclaredConstructor().newInstance());
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
