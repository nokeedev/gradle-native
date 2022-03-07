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
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

final class LoadAllSamples implements Action<NamedDomainObjectContainer<Sample>> {
	private final Path samplesDirectory;

	public LoadAllSamples(Path samplesDirectory) {
		this.samplesDirectory = samplesDirectory;
	}

	@Override
	public void execute(NamedDomainObjectContainer<Sample> samples) {
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(samplesDirectory)) {
			for (Path sampleDirectory : directoryStream) {
				if (Files.isDirectory(sampleDirectory) && Files.exists(sampleDirectory.resolve("README.adoc"))) {
					samples.register(sampleDirectory.getFileName().toString(), sample -> {
						sample.getSampleDirectory().fileValue(sampleDirectory.toFile());
					});
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
