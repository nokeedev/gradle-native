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

import joptsimple.OptionParser;
import org.gradle.profiler.GradleBuildInvoker;
import org.gradle.profiler.Profiler;
import org.gradle.profiler.ProfilerFactory;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import static dev.nokee.platform.cpp.GradleProfilerUtils.createProfiler;
import static dev.nokee.platform.cpp.GradleProfilerUtils.defaultProfiler;

public final class GradleProfilerBuildExperimentExecutorBuilder {
	private static final GradleProfilerBuildExperimentExecutorMeasurement GARBAGE_COLLECTION_MEASUREMENT = new GradleProfilerGarbageCollectionMeasurement();
	private static final GradleProfilerBuildExperimentExecutorMeasurement CONFIGURATION_TIME_MEASUREMENT = new GradleProfilerConfigurationTimeMeasurement();
	private String displayName;
	private GradleBuildInvoker invoker;
	private final Set<GradleProfilerBuildExperimentExecutorMeasurement> measurements = new LinkedHashSet<>();
	private Profiler profiler = defaultProfiler();
	private Path outputDirectory;

	GradleProfilerBuildExperimentExecutorBuilder() {}

	public GradleProfilerBuildExperimentExecutorBuilder displayName(String displayName) {
		this.displayName = displayName;
		return this;
	}

	public GradleProfilerBuildExperimentExecutorBuilder useToolingApiClient() {
		this.invoker = GradleBuildInvoker.ToolingApi;
		return this;
	}

	public GradleProfilerBuildExperimentExecutorBuilder measureGarbageCollection() {
		this.measurements.add(GARBAGE_COLLECTION_MEASUREMENT);
		return this;
	}

	public GradleProfilerBuildExperimentExecutorBuilder measureConfigurationTime() {
		this.measurements.add(CONFIGURATION_TIME_MEASUREMENT);
		return this;
	}

	public GradleProfilerBuildExperimentExecutorBuilder measureBuildOperation(String name) {
		this.measurements.add(new GradleProfilerBuildOperationMeasurement(name));
		return this;
	}

	public GradleProfilerBuildExperimentExecutorBuilder outputDirectory(Path outputDirectory) {
		this.outputDirectory = outputDirectory;
		return this;
	}

	public GradleProfilerBuildExperimentExecutorBuilder profileUsingJavaFlightRecorder() {
		this.profiler = createProfiler("jfr");
		return this;
	}

	GradleProfilerBuildExperimentExecutor build() {
		Objects.requireNonNull(displayName);

		if (invoker == null) {
			this.invoker = GradleBuildInvoker.ToolingApi;
		}

		return new GradleProfilerBuildExperimentExecutor(new GradleProfilerBuildExperimentExecutorContext(displayName, invoker, measurements, profiler, outputDirectory));
	}

}
