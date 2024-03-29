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

import dev.gradleplugins.runnerkit.GradleDistribution;
import dev.gradleplugins.runnerkit.providers.CommandLineArgumentsProvider;
import dev.gradleplugins.runnerkit.providers.GradleDistributionProvider;
import dev.gradleplugins.runnerkit.providers.GradleExecutionProvider;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.gradle.util.GradleVersion;

import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
public class GradleInvocationSpec implements BuildExperimentInvocationSpec {
	@With(AccessLevel.PRIVATE) private GradleExecutionProvider<GradleDistribution> gradleDistribution = GradleDistributionProvider.version(GradleVersion.current().getVersion());
	private GradleExecutionProvider<List<String>> arguments = CommandLineArgumentsProvider.empty();

	public GradleInvocationSpec() {}

	public GradleInvocationSpec withGradleVersion(String versionNumber) {
		return withGradleDistribution(GradleDistributionProvider.version(versionNumber));
	}

	public GradleInvocationSpec withArguments(String... arguments) {
		return new GradleInvocationSpec(gradleDistribution, CommandLineArgumentsProvider.of(Arrays.asList(arguments)));
	}
}
