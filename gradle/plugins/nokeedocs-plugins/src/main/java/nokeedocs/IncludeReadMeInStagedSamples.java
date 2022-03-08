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
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.tasks.Sync;

final class IncludeReadMeInStagedSamples implements Action<NamedDomainObjectSet<Sample>> {
	private final Project project;

	public IncludeReadMeInStagedSamples(Project project) {
		this.project = project;
	}

	@Override
	public void execute(NamedDomainObjectSet<Sample> samples) {
		project.getTasks().named("assembleSamples", Sync.class, task -> {
			samples.all(sample -> {
				task.from(sample.getSampleDirectory().file("README.adoc"), spec -> {
					spec.rename(new RenameReadMeDotAdocToIndexDotAdocTransformer());
					spec.into(sample.getPermalink());
				});
			});
		});
	}
}
