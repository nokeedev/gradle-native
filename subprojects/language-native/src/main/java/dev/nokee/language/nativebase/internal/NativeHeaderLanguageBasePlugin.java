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

import dev.nokee.language.base.internal.LanguagePropertiesAware;
import dev.nokee.language.base.internal.SourcePropertyName;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.nativebase.HasHeaders;
import dev.nokee.language.nativebase.NativeSourceSetComponentDependencies;
import dev.nokee.platform.base.DependencyAwareComponent;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sources;
import static dev.nokee.model.internal.ModelElementAction.withElement;
import static dev.nokee.model.internal.TypeFilteringAction.ofType;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;

public class NativeHeaderLanguageBasePlugin implements Plugin<Project> {
	public static final SourcePropertyName PRIVATE_HEADERS = () -> "privateHeaders";
	public static final SourcePropertyName PUBLIC_HEADERS = () -> "publicHeaders";

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageBasePlugin.class);

		model(project, objects()).configureEach(ofType(PublicHeadersMixIn.class, it -> {
			val publicHeaders = project.getObjects().newInstance(NativeHeaderProperty.class, "publicHeaders");
			publicHeaders.getVisibility().set(NativeHeaderProperty.BasicVisibility.Public);
			it.getSourceProperties().add(publicHeaders);
		}));

		sources(project).configureEach(new HeaderSearchPathsConfigurationRegistrationAction<>(project.getObjects()));
		sources(project).configureEach(new AttachHeaderSearchPathsToCompileTaskRule<>());
		sources(project).configureEach(new NativeCompileTaskDefaultConfigurationRule<>());
		sources(project).configureEach(sourceSet -> {
			if (sourceSet instanceof HasHeaderSearchPaths && sourceSet instanceof DependencyAwareComponent && ((DependencyAwareComponent<?>) sourceSet).getDependencies() instanceof NativeSourceSetComponentDependencies) {
				((HasHeaderSearchPaths) sourceSet).getHeaderSearchPaths().extendsFrom(((NativeSourceSetComponentDependencies) ((DependencyAwareComponent<?>) sourceSet).getDependencies()).getCompileOnly());
			}
		});

		sources(project).configureEach(ofType(HasHeaders.class, withElement((element, sourceSet) -> {
			sourceSet.getHeaders().from((Callable<Object>) () -> {
				return element.getParents().flatMap(it -> {
					return it.safeAs(LanguagePropertiesAware.class).map(a -> a.getSourceProperties().findByName("publicHeaders")).map(Stream::of).getOrElse(Stream.empty());
				}).findFirst().map(a -> (Iterable<?>) a.getSource()).orElse(Collections.emptyList());
			});
		})));
		sources(project).configureEach(ofType(HasHeaders.class, withElement((element, sourceSet) -> {
			sourceSet.getHeaders().from((Callable<Object>) () -> {
				return element.getParents().flatMap(it -> {
					return it.safeAs(LanguagePropertiesAware.class).map(a -> a.getSourceProperties().findByName("privateHeaders")).map(Stream::of).getOrElse(Stream.empty());
				}).findFirst().map(a -> (Iterable<?>) a.getSource()).orElse(Collections.emptyList());
			});
		})));
	}
}
