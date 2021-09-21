/*
 * Copyright 2020 the original author or authors.
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
package dev.gradleplugins.exemplarkit;

import java.io.File;
import java.util.Objects;

public final class ExemplarRunner {
	private File workingDirectory;
	private Exemplar exemplar;
	private ExemplarExecutor executor;

	private ExemplarRunner(ExemplarExecutor executor) {
		this.executor = executor;
	}

	public static ExemplarRunner create(ExemplarExecutor executor) {
		return new ExemplarRunner(executor);
	}

	public ExemplarRunner inDirectory(File workingDirectory) {
		this.workingDirectory = Objects.requireNonNull(workingDirectory, "Please specify a non-null working directory.");
		return this;
	}

	public File getWorkingDirectory() {
		return workingDirectory;
	}

	public ExemplarRunner using(Exemplar exemplar) {
		this.exemplar = Objects.requireNonNull(exemplar, "Please specify a non-null exemplar.");
		return this;
	}

	public ExemplarExecutionResult run() throws InvalidRunnerConfigurationException {
		if (workingDirectory == null) {
			throw new InvalidRunnerConfigurationException("Please specify a working directory using ExemplarRunner#inDirectory(File).");
		}

		if (exemplar == null) {
			throw new InvalidRunnerConfigurationException("Please specify an exemplar using ExemplarRunner#using(Exemplar).");
		}

		return executor.run(new ExemplarExecutionContext(workingDirectory, exemplar));
	}
}
