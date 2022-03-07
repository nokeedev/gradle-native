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
package nokeedocs;

import dev.gradleplugins.dockit.samples.Sample;
import dev.gradleplugins.dockit.samples.SampleArchiveArtifact;
import org.gradle.api.file.CopySpec;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

final class AllSampleArchiveArtifacts implements BiConsumer<Sample, CopySpec> {
	@Override
	public void accept(Sample sample, CopySpec spec) {
		spec.from((Callable<Object>) () -> sample.getArtifacts().withType(SampleArchiveArtifact.class).stream().map(SampleArchiveArtifact::getArchiveFile).collect(Collectors.toList()));
	}
}
