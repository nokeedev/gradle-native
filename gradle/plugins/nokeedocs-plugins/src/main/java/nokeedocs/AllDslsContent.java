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

import dev.gradleplugins.dockit.samples.Dsl;
import dev.gradleplugins.dockit.samples.Sample;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.CopySpec;
import org.gradle.api.tasks.Sync;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;

class AllDslsContent implements BiConsumer<Sample, CopySpec> {
	@Override
	public void accept(Sample sample, CopySpec rootSpec) {
		rootSpec.into(sample.getPermalink(), spec -> {
			for (Dsl dsl : sample.getDsls().get()) {
				spec.from(sample.getSampleDirectory().dir(dsl.getName()), it -> {
					it.exclude(".gradle");
					it.into(dsl.getName());
				});
			}
		});

		((Task) rootSpec).doLast(new Action<Task>() {
			@Override
			public void execute(Task task) {
				try {
					for (Dsl dsl : sample.getDsls().get()) {
						final Path ignoreFile = ((Sync) task).getDestinationDir().toPath()
							.resolve(sample.getPermalink().get() + "/" + dsl.getName() + "/.jbakeignore");
						if (Files.notExists(ignoreFile)) {
							Files.createFile(ignoreFile);
						}
					}
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}
		});
	}
}
