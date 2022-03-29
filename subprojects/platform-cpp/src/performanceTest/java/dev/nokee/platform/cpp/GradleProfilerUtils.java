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
import org.gradle.profiler.Profiler;
import org.gradle.profiler.ProfilerFactory;

import java.util.Collections;
import java.util.Optional;

final class GradleProfilerUtils {
	private static final String PROFILER_KEY = "org.gradle.performance.profiler";

	private GradleProfilerUtils() {}

	private static Optional<String> profilerName() {
		return Optional.ofNullable(System.getProperty(PROFILER_KEY)).filter(it -> !it.isEmpty());
	}

	public static Profiler defaultProfiler() {
		return profilerName().map(GradleProfilerUtils::createProfiler).orElse(Profiler.NONE);
	}

	public static Profiler createProfiler(String profilerName) {
		OptionParser optionParser = new OptionParser();
		optionParser.accepts("profiler");
		ProfilerFactory.configureParser(optionParser);
		ProfilerFactory profilerFactory = ProfilerFactory.of(Collections.singletonList(profilerName));
		String[] options = profilerName.equals("jprofiler")
			? new String[] {"--profile", "jprofiler", "--jprofiler-home", System.getenv("JPROFILER_HOME")}
			: new String[] {};
		return profilerFactory.createFromOptions(optionParser.parse(options));
	}
}
