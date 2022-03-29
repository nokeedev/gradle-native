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
package dev.nokee.platform.cpp.results;

import dev.nokee.platform.cpp.BuildExperimentResult;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;

public final class PerformanceTestResult implements Iterable<BuildExperimentResult> {
	private final PerformanceExperiment experiment;

	// TODO: Each result should be named
	private final Map<String, BuildExperimentResult> results; // TODO: should include BuildDisplayInfo
	//  experiment info (action to run/action to clean) + invocation spec (args, uses deamon, gradle opts, Gradle distribution)

	// TODO: Include unique ID to identify this test (testId)
	// TODO: include the name of the scenario result
	// TODO: Include testClass
	// TODO: Include testProject

	// TODO: Operating system
	// TODO: JVM...

	// TODO: Start time/end time?


	public PerformanceTestResult(PerformanceExperiment experiment, Map<String, BuildExperimentResult> results) {
		this.experiment = experiment;
		this.results = results;
	}

	public PerformanceExperiment getPerformanceExperiment() {
		return experiment;
	}

	@Override
	public Iterator<BuildExperimentResult> iterator() {
		return results.values().iterator();
	}

	public void forEach(BiConsumer<? super String, ? super BuildExperimentResult> action) {
		results.forEach(action);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private final Map<String, BuildExperimentResult> experiments = new HashMap<>();
		private String testClass;
		private String testProject;
		private String testCase;

		public Builder experiment(String displayName, BuildExperimentResult result) {
			experiments.put(displayName, result);
			return this;
		}

		public Builder testClass(String testClass) {
			this.testClass = testClass;
			return this;
		}

		public Builder testProject(String testProject) {
			this.testProject = testProject;
			return this;
		}

		public Builder testCase(String testCase) {
			this.testCase = testCase;
			return this;
		}

		public PerformanceTestResult build() {
			return new PerformanceTestResult(new PerformanceExperiment(testProject, new PerformanceScenario(testClass, testCase)), experiments);
		}
	}
}
