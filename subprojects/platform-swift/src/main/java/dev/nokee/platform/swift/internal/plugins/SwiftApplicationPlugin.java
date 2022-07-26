/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.swift.internal.plugins;

import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.language.swift.internal.plugins.SwiftLanguageBasePlugin;
import dev.nokee.language.swift.internal.plugins.SwiftSourceSetTag;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.ModelBackedBinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedDependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.assembletask.HasAssembleTaskMixIn;
import dev.nokee.platform.base.internal.ModelBackedHasBaseNameMixIn;
import dev.nokee.platform.base.internal.developmentvariant.HasDevelopmentVariantMixIn;
import dev.nokee.platform.base.internal.ModelBackedNamedMixIn;
import dev.nokee.platform.base.internal.ModelBackedSourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedTaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedVariantAwareComponentMixIn;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetBuildTypeAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetLinkageAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetMachineAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.NativeApplicationComponentModelRegistrationFactory;
import dev.nokee.platform.nativebase.internal.dependencies.ModelBackedNativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.swift.HasSwiftSourceSet;
import dev.nokee.platform.swift.SwiftApplication;
import dev.nokee.platform.swift.SwiftApplicationSources;
import groovy.lang.Closure;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;
import org.gradle.util.GUtil;

import javax.inject.Inject;

import static dev.nokee.language.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.platform.base.internal.BaseNameActions.baseName;
import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.finalizeModelNodeOf;
import static org.gradle.util.ConfigureUtil.configureUsing;

public class SwiftApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public SwiftApplicationPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(SwiftCompilerPlugin.class);

		// Create the component
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(SwiftLanguageBasePlugin.class);
		val componentProvider = project.getExtensions().getByType(ModelRegistry.class).register(swiftApplication("main", project)).as(SwiftApplication.class);
		componentProvider.configure(baseName(convention(GUtil.toCamelCase(project.getName()))));
		val extension = componentProvider.get();

		// Other configurations
		project.afterEvaluate(finalizeModelNodeOf(componentProvider));

		project.getExtensions().add(SwiftApplication.class, EXTENSION_NAME, extension);
	}

	public static ModelRegistration swiftApplication(String name, Project project) {
		val identifier = ComponentIdentifier.builder().name(ComponentName.of(name)).displayName("Swift application").withProjectIdentifier(ProjectIdentifier.of(project)).build();
		return new NativeApplicationComponentModelRegistrationFactory(DefaultSwiftApplication.class, project).create(identifier).withComponent(tag(SwiftSourceSetTag.class)).build();
	}

	public static abstract class DefaultSwiftApplication implements SwiftApplication
		, ModelBackedDependencyAwareComponentMixIn<NativeApplicationComponentDependencies, ModelBackedNativeApplicationComponentDependencies>
		, ModelBackedVariantAwareComponentMixIn<NativeApplication>
		, ModelBackedSourceAwareComponentMixIn<SwiftApplicationSources, SwiftApplicationSourcesAdapter>
		, ModelBackedBinaryAwareComponentMixIn
		, ModelBackedTaskAwareComponentMixIn
		, HasDevelopmentVariantMixIn<NativeApplication>
		, ModelBackedTargetMachineAwareComponentMixIn
		, ModelBackedTargetBuildTypeAwareComponentMixIn
		, ModelBackedTargetLinkageAwareComponentMixIn
		, ModelBackedHasBaseNameMixIn
		, ModelBackedNamedMixIn
		, HasAssembleTaskMixIn
	{
		@Override
		public SwiftSourceSet getSwiftSources() {
			return ((HasSwiftSourceSet) sourceViewOf(this)).getSwift().get();
		}

		@Override
		public void swiftSources(Action<? super SwiftSourceSet> action) {
			((HasSwiftSourceSet) sourceViewOf(this)).getSwift().configure(action);
		}

		@Override
		public void swiftSources(@SuppressWarnings("rawtypes") Closure closure) {
			swiftSources(configureUsing(closure));
		}
	}
}
