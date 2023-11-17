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

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.nativebase.HasHeaders;
import dev.nokee.language.nativebase.HasPublicHeaders;
import dev.nokee.language.nativebase.NativeSourceSetComponentDependencies;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.SourceAwareComponent;
import dev.nokee.platform.base.View;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.plugins.ExtensionAware;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Callable;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sources;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.components;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;

public class NativeHeaderLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageBasePlugin.class);

		sources(project).configureEach(new HeaderSearchPathsConfigurationRegistrationAction<>(project.getObjects()));
		sources(project).configureEach(new AttachHeaderSearchPathsToCompileTaskRule<>());
		sources(project).configureEach(new NativeCompileTaskDefaultConfigurationRule<>());
		sources(project).configureEach(sourceSet -> {
			if (sourceSet instanceof HasHeaderSearchPaths && sourceSet instanceof DependencyAwareComponent && ((DependencyAwareComponent<?>) sourceSet).getDependencies() instanceof NativeSourceSetComponentDependencies) {
				((HasHeaderSearchPaths) sourceSet).getHeaderSearchPaths().extendsFrom(((NativeSourceSetComponentDependencies) ((DependencyAwareComponent<?>) sourceSet).getDependencies()).getCompileOnly());
			}
		});

		variants(project).configureEach(variant -> {
			// TODO: check if it's a native variant?
			if (variant instanceof SourceAwareComponent && ((SourceAwareComponent<?>) variant).getSources() instanceof View) {
				@SuppressWarnings("unchecked")
				final View<LanguageSourceSet> sources = (View<LanguageSourceSet>) ((SourceAwareComponent<?>) variant).getSources();
				sources.configureEach(sourceSet -> {
					if (sourceSet instanceof HasHeaders) {
						((HasHeaders) sourceSet).getHeaders().from((Callable<Object>) () -> {
							ModelNodes.safeOf(variant).ifPresent(ModelStates::finalize);
							return Optional.ofNullable(((ExtensionAware) variant).getExtensions().findByName("privateHeaders")).orElse(Collections.emptyList());
						});
					}
				});
			}
		});

		components(project).configureEach(target -> {
			ConfigurableFileCollection sources = null;
			// TODO: mixin on native library
			if (target instanceof HasPublicHeaders) {
				sources = ((HasPublicHeaders) target).getPublicHeaders();
			}

			if (sources != null) {
				((ExtensionAware) target).getExtensions().add(ConfigurableFileCollection.class, "publicHeaders", sources);
			}
		});
		components(project).configureEach(new UseConventionalLayout<>("publicHeaders", "src/%s/public"));
		components(project).configureEach(new ExtendsFromParentNativeSourcesRule<>("publicHeaders"));
		components(project).configureEach(variant -> {
			// TODO: check if it's a native variant?
			if (variant instanceof SourceAwareComponent && ((SourceAwareComponent<?>) variant).getSources() instanceof View) {
				@SuppressWarnings("unchecked")
				final View<LanguageSourceSet> sources = (View<LanguageSourceSet>) ((SourceAwareComponent<?>) variant).getSources();
				sources.configureEach(sourceSet -> {
					if (sourceSet instanceof HasHeaders) {
						((HasHeaders) sourceSet).getHeaders().from((Callable<Object>) () -> {
							ModelNodes.safeOf(variant).ifPresent(ModelStates::finalize);
							return Optional.ofNullable(((ExtensionAware) variant).getExtensions().findByName("publicHeaders")).orElse(Collections.emptyList());
						});
					}
				});
			}
		});
	}
}
