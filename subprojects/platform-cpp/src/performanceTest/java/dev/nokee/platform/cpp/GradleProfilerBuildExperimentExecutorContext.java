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

import org.gradle.profiler.GradleBuildInvoker;
import org.gradle.profiler.Profiler;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

final class GradleProfilerBuildExperimentExecutorContext {
	private final String displayName;
	private final GradleBuildInvoker invoker;
	private final Set<GradleProfilerBuildExperimentExecutorMeasurement> measurements;
	private final Profiler profiler;
	private final Path outputDirectory;

	GradleProfilerBuildExperimentExecutorContext(String displayName, GradleBuildInvoker invoker, Set<GradleProfilerBuildExperimentExecutorMeasurement> measurements, Profiler profiler, Path outputDirectory) {
		this.displayName = Objects.requireNonNull(displayName);
		this.invoker = invoker;
		this.measurements = measurements;
		this.profiler = profiler;
		this.outputDirectory = outputDirectory;
	}

	public String getDisplayName() {
		return displayName;
	}

	public GradleBuildInvoker getInvoker() {
		return invoker;
	}

	public Stream<GradleProfilerBuildExperimentExecutorMeasurement> getMeasurements() {
		return measurements.stream();
	}

	public Profiler getProfiler() {
		return profiler;
	}

	public boolean usesDaemon() {
		return invoker.getClient().isUsesDaemon();
	}

	public Path getOutputDirectory() {
		return outputDirectory;
	}
}
