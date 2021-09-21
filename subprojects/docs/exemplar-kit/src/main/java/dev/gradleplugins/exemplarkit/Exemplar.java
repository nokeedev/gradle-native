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

import lombok.ToString;

import java.io.File;
import java.util.*;

import static java.util.Objects.requireNonNull;

@ToString
public final class Exemplar {
	private final Sample sample;
	private final List<Step> steps;

	private Exemplar(Sample sample, List<Step> steps) {
		this.sample = sample;
		this.steps = steps;
	}

	public Sample getSample() {
		return sample;
	}

	public List<Step> getSteps() {
		return Collections.unmodifiableList(steps);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Sample sample = Sample.empty();
		private final List<Step> steps = new ArrayList<>();

		public Builder fromArchive(File archiveFile) {
			this.sample = Sample.fromArchive(archiveFile);
			return this;
		}

		public Builder fromDirectory(File sampleDirectory) {
			this.sample = Sample.fromDirectory(sampleDirectory);
			return this;
		}

		public Builder step(Step step) {
			steps.add(requireNonNull(step, "Step cannot be null."));
			return this;
		}

		public Builder step(Step.Builder stepBuilder) {
			steps.add(requireNonNull(stepBuilder, "Step builder cannot be null.").build());
			return this;
		}

		public Exemplar build() {
			return new Exemplar(sample, steps);
		}
	}
}
