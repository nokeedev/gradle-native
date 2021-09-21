/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.publish.bintray.internal;

import dev.nokee.utils.TaskActionUtils;
import dev.nokee.utils.TaskUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

public class BintrayPublishPlugin implements Plugin<Project> {
	private final WorkerExecutor workers;

	@Inject
	public BintrayPublishPlugin(WorkerExecutor workers) {
		this.workers = workers;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("maven-publish");

		project.afterEvaluate(this::configurePublishToBintrayRepositoryTasks);
	}

	private static TaskCollection<PublishToMavenRepository> mavenPublishTasksOf(Project project) {
		return project.getTasks().withType(PublishToMavenRepository.class);
	}

	private static PublishingExtension publishingOf(Project project) {
		return project.getExtensions().getByType(PublishingExtension.class);
	}

	private void configurePublishToBintrayRepositoryTasks(Project project) {
		val factory = new BintrayRepositoryFactory(new BintrayCredentialsFactory(project.getProviders()), () -> project.getGroup().toString());
		publishingOf(project).getRepositories().withType(MavenArtifactRepository.class).stream().filter(BintrayPublishPlugin::isBintrayRepository).forEach(repository -> {
			val bintrayRepository = factory.create(repository);
			mavenPublishTasksOf(project).configureEach(configurePublishingOnlyIfBintrayRepository(repository, bintrayRepository, project, workers));
		});
	}

	private static Action<PublishToMavenRepository> configurePublishingOnlyIfBintrayRepository(MavenArtifactRepository repository, BintrayRepository bintrayRepository, Project project, WorkerExecutor workers) {
		return task -> {
			task.dependsOn(executes(() -> {
				if (task.getRepository().equals(repository)) {
					task.getInputs().property("bintrayRepository", bintrayRepository);

					val stagingRepositoryLocation = project.getLayout().getBuildDirectory().dir(TaskUtils.temporaryDirectoryPath(task) + "/repository");
					val publication = new BintrayPublication(stagingRepositoryLocation::get, () -> task.getPublication().getVersion());

					task.setRepository(project.getRepositories().maven(it -> {
						it.setUrl(stagingRepositoryLocation);
						it.mavenContent(content -> {
							content.excludeGroupByRegex(".*");
						});
					}));

					// Reverse order prepending actions...
					task.doFirst(TaskActionUtils.deleteDirectories(stagingRepositoryLocation));

					// The actual Bintray publishing
					task.doLast(new PublishToBintrayRepositoryAction(bintrayRepository, publication, new BintrayRestClient(workers)));
				}
			}));
		};
	}

	private static Callable<List<Object>> executes(Runnable runnable) {
		return () -> {
			runnable.run();
			return Collections.emptyList();
		};
	}

	private static boolean isBintrayRepository(MavenArtifactRepository repository) {
		return Objects.equals(repository.getUrl().getHost(), "dl.bintray.com");
	}
}
