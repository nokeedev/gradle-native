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
package dev.nokee.language.nativebase.internal;

import dev.nokee.language.base.HasCompileTask;
import dev.nokee.language.base.HasSource;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.LanguageSupportSpec;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.nativebase.HasHeaders;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.utils.TaskDependencyUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.language.swift.tasks.SwiftCompile;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sources;
import static dev.nokee.model.internal.TypeFilteringAction.ofType;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;

public class LanguageNativeBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ComponentModelBasePlugin.class); // TODO: Revisit if this is a good idea
		project.getPluginManager().apply(LanguageBasePlugin.class);

		// Attach native source to compile task
		sources(project).configureEach(sourceSet -> {
			if (sourceSet instanceof HasSource && sourceSet instanceof HasCompileTask) {
				((HasCompileTask) sourceSet).getCompileTask().configure(task -> {
					if (task instanceof AbstractNativeCompileTask) {
						((AbstractNativeCompileTask) task).getSource().from(((HasSource) sourceSet).getSource().getAsFileTree());
					} else if (task instanceof SwiftCompile) {
						((SwiftCompile) task).getSource().from(((HasSource) sourceSet).getSource().getAsFileTree());
					}
				});
			}
		});

		sources(project).configureEach(new HasNativeCompileTaskMixInRule<>(new DefaultNativeToolChainSelector(((ProjectInternal) project).getModelRegistry(), project.getProviders())));

		sources(project).withType(BaseNativeSourceSetSpec.class).configureEach(sourceSet -> {
			sourceSet.getBuildDependencies().add(sourceSet.getSource().getBuildDependencies());
			sourceSet.getBuildDependencies().add(TaskDependencyUtils.of(sourceSet.getCompileTask()));
			if (sourceSet instanceof HasHeaders) {
				sourceSet.getBuildDependencies().add(((HasHeaders) sourceSet).getHeaders().getBuildDependencies());
			}
		});

		model(project, objects()).configureEach(ofType(NativeLanguageSourceSetAware.class, it -> {
			it.getLanguageImplementations().all(langImpl -> {
				langImpl.registerSourceSet((name, sourceSetType) -> model(project, registryOf(LanguageSourceSet.class)).register(it.getIdentifier().child(name), sourceSetType));
			});
		}));

		project.getPlugins().withType(NativeLanguageSupportPlugin.class, nativeLangPlugin -> {
			model(project, objects()).configureEach(ofType(NativeSourcesAware.class, nativeLangPlugin::registerImplementation));
		});
		project.getPlugins().withType(NativeLanguageSupportPlugin.class, nativeLangPlugin -> {
			model(project, objects()).configureEach(ofType(NativeLanguageSourceSetAware.class, nativeLangPlugin::registerImplementation));
		});

		model(project, objects()).configureEach(ofType(NativeSourcesAware.class, it -> {
			it.getParents().filter(t -> t.instanceOf(LanguageSupportSpec.class))
				.forEach(t -> t.safeAs(LanguageSupportSpec.class).get().getLanguageImplementations().withType(NativeLanguageImplementation.class, a -> it.getLanguageImplementations().add(a)));
		}));
		model(project, objects()).configureEach(ofType(NativeLanguageSourceSetAware.class, it -> {
			it.getParents().filter(t -> t.instanceOf(LanguageSupportSpec.class))
				.forEach(t -> t.safeAs(LanguageSupportSpec.class).get().getLanguageImplementations().withType(NativeLanguageImplementation.class, a -> it.getLanguageImplementations().add(a)));
		}));
	}
}
