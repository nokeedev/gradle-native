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

import dev.nokee.platform.cpp.measure.DataSeries;
import dev.nokee.platform.cpp.measure.Duration;
import dev.nokee.platform.cpp.measure.MeasuredOperation;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Collectors;

final class DefaultBuildExperimentResult implements BuildExperimentResult {
	private final BuildExperimentExecutionResult delegate;
	private final BuildExperimentAction buildAction;
	@Nullable
	private final BuildExperimentAction cleanAction;

	public DefaultBuildExperimentResult(BuildExperimentExecutionResult delegate, BuildExperimentAction buildAction, @Nullable BuildExperimentAction cleanAction) {
		this.delegate = delegate;
		this.buildAction = buildAction;
		this.cleanAction = cleanAction;
	}

	// TODO: Should be PerformanceScenario....
	//   Experiment is an aggregation of build experiment (scenario)

	// TODO: Should not be public, maybe...
	public BuildExperimentExecutionResult getExecutionResult() {
		return delegate;
	}

	@Override
	public DataSeries<Duration> getTotalTime() {
		return new DataSeries<>(delegate.getMeasurements().map(MeasuredOperation::getTotalTime).collect(Collectors.toList()));
	}

	@Override
	public BuildExperimentAction getBuildAction() {
		return buildAction;
	}

	@Override
	public Optional<BuildExperimentAction> getCleanAction() {
		return Optional.ofNullable(cleanAction);
	}
}
