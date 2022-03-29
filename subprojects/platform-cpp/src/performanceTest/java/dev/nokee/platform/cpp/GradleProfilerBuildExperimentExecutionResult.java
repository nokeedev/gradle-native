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

import dev.nokee.platform.cpp.measure.Duration;
import dev.nokee.platform.cpp.measure.MeasuredOperation;
import dev.nokee.platform.cpp.results.GradleProfilerResult;
import org.gradle.profiler.InvocationSettings;
import org.gradle.profiler.ScenarioDefinition;
import org.gradle.profiler.ScenarioInvoker;
import org.gradle.profiler.result.BuildInvocationResult;

import java.util.List;
import java.util.stream.Stream;

final class GradleProfilerBuildExperimentExecutionResult<S extends ScenarioDefinition, R extends BuildInvocationResult> implements BuildExperimentExecutionResult, GradleProfilerResult<S, R> {
	private final List<R> invocationResults;
	private final S scenarioDefinition;
	private final ScenarioInvoker<S, R> scenarioInvoker;
	private final InvocationSettings invocationSettings;

	public GradleProfilerBuildExperimentExecutionResult(List<R> invocationResults, S scenarioDefinition, ScenarioInvoker<S, R> scenarioInvoker, InvocationSettings invocationSettings) {
		this.invocationResults = invocationResults;
		this.scenarioDefinition = scenarioDefinition;
		this.scenarioInvoker = scenarioInvoker;
		this.invocationSettings = invocationSettings;
	}

	@Override
	public Stream<MeasuredOperation> getMeasurements() {
		return invocationResults.stream().skip(scenarioDefinition.getBuildCount()).map(invocationResult -> new MeasuredOperation().withTotalTime(Duration.millis(invocationResult.getExecutionTime().toMillis())));
	}

	@Override
	public int getWarmUpRuns() {
		return invocationSettings.getWarmUpCount();
	}

	@Override
	public int getMeasurementRuns() {
		return invocationSettings.getBuildCount();
	}

	public List<R> getInvocationResults() {
		return invocationResults;
	}

	public S getScenarioDefinition() {
		return scenarioDefinition;
	}

	public ScenarioInvoker<S, R> getScenarioInvoker() {
		return scenarioInvoker;
	}

	public InvocationSettings getInvocationSettings() {
		return invocationSettings;
	}
}
