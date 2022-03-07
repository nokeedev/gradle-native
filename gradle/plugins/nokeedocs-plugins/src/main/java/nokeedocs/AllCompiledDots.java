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

import dev.gradleplugins.dockit.common.TaskNameFactory;
import nokeedocs.tasks.DotCompile;
import org.gradle.api.Project;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

final class AllCompiledDots<T extends ExtensionAware & TaskNameFactory> implements BiConsumer<T, CopySpec> {
	private final Project project;

	public AllCompiledDots(Project project) {
		this.project = project;
	}

	@Override
	public void accept(T sample, CopySpec spec) {
		spec.from(compileDots(project, sample));
	}

	private static <T extends ExtensionAware & TaskNameFactory> Callable<Object> compileDots(Project project, T sample) {
		return new Callable<Object>() {
			private TaskProvider<DotCompile> createdTask = null;

			@Override
			public Object call() throws Exception {
				if (createdTask == null) {
					createdTask = project.getTasks().register(sample.taskName("compile", "dot"), DotCompile.class, task -> {
						task.getOutputDirectory().value(project.getLayout().getBuildDirectory().dir("tmp/" + task.getName())).disallowChanges();
						task.getSource().setDir(sourceDirectory(sample)).include("**/*.dot");
					});
				}
				return createdTask;
			}
		};
	}

	private static Provider<Directory> sourceDirectory(ExtensionAware target) {
		return (Provider<Directory>) target.getExtensions().getByName("sourceDirectory");
	}
}
