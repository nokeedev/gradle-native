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
package dev.nokee.platform.objectivecpp.internal.plugins;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.BaseLanguageSourceSetProjection;
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.cpp.CppHeaderSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppLanguageBasePlugin;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.nativebase.HasHeadersSourceSet;
import dev.nokee.platform.nativebase.HasPublicSourceSet;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.*;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.objectivecpp.HasObjectiveCppSourceSet;
import dev.nokee.platform.objectivecpp.ObjectiveCppLibrary;
import dev.nokee.platform.objectivecpp.ObjectiveCppLibrarySources;
import groovy.lang.Closure;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.*;
import static dev.nokee.platform.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.*;
import static org.gradle.util.ConfigureUtil.configureUsing;

public class ObjectiveCppLibraryPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "library";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public ObjectiveCppLibraryPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		// Create the component
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(ObjectiveCppLanguageBasePlugin.class);
		val componentProvider = project.getExtensions().getByType(ModelRegistry.class).register(objectiveCppLibrary("main", project)).as(ObjectiveCppLibrary.class);
		componentProvider.configure(configureUsingProjection(DefaultNativeLibraryComponent.class, baseNameConvention(project.getName()).andThen(configureBuildVariants())));
		val extension = componentProvider.get();

		// Other configurations
		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetLinkageRule.class, extension.getTargetLinkages(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(finalizeModelNodeOf(componentProvider));

		project.getExtensions().add(ObjectiveCppLibrary.class, EXTENSION_NAME, extension);
	}

	public static ModelRegistration objectiveCppLibrary(String name, Project project) {
		return new NativeLibraryComponentModelRegistrationFactory(ObjectiveCppLibrary.class, DefaultObjectiveCppLibrary.class, project, (entity, path) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);

			// TODO: Should be created using ObjectiveCppSourceSetSpec
			registry.register(ModelRegistration.builder()
				.withComponent(path.child("objectiveCpp"))
				.withComponent(IsLanguageSourceSet.tag())
				.withComponent(managed(of(ObjectiveCppSourceSet.class)))
				.withComponent(managed(of(BaseLanguageSourceSetProjection.class)))
				.build());

			// TODO: Should be created using CppHeaderSetSpec
			registry.register(ModelRegistration.builder()
				.withComponent(path.child("public"))
				.withComponent(IsLanguageSourceSet.tag())
				.withComponent(managed(of(CppHeaderSet.class)))
				.withComponent(managed(of(BaseLanguageSourceSetProjection.class)))
				.build());

			// TODO: Should be created using CppHeaderSetSpec
			registry.register(ModelRegistration.builder()
				.withComponent(path.child("headers"))
				.withComponent(IsLanguageSourceSet.tag())
				.withComponent(managed(of(CppHeaderSet.class)))
				.withComponent(managed(of(BaseLanguageSourceSetProjection.class)))
				.build());

			registry.register(project.getExtensions().getByType(ComponentSourcesPropertyRegistrationFactory.class).create(path.child("sources"), ObjectiveCppLibrarySources.class));

			project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), ModelComponentReference.ofAny(ModelComponentType.projectionOf(ObjectiveCppSourceSet.class)), (e, p, ignored, projection) -> {
				if (path.isDescendant(p)) {
					withConventionOf(maven(ComponentName.of(name)), defaultObjectiveCppGradle(ComponentName.of(name))).accept(ModelNodeUtils.get(e, LanguageSourceSet.class));
				}
			}));
		}).create(ComponentIdentifier.builder().name(ComponentName.of(name)).displayName("Objective-C++ library").withProjectIdentifier(ProjectIdentifier.of(project)).build());
	}

	public static abstract class DefaultObjectiveCppLibrary implements ObjectiveCppLibrary
		, ModelBackedDependencyAwareComponentMixIn<NativeLibraryComponentDependencies>
		, ModelBackedVariantAwareComponentMixIn<NativeLibrary>
		, ModelBackedSourceAwareComponentMixIn<ObjectiveCppLibrarySources>
		, ModelBackedBinaryAwareComponentMixIn
		, ModelBackedTaskAwareComponentMixIn
		, ModelBackedHasDevelopmentVariantMixIn<NativeLibrary>
		, ModelBackedTargetMachineAwareComponentMixIn
		, ModelBackedTargetBuildTypeAwareComponentMixIn
		, ModelBackedTargetLinkageAwareComponentMixIn
	{
		@Override
		public ObjectiveCppSourceSet getObjectiveCppSources() {
			return ((HasObjectiveCppSourceSet) sourceViewOf(this)).getObjectiveCpp().get();
		}

		@Override
		public void objectiveCppSources(Action<? super ObjectiveCppSourceSet> action) {
			((HasObjectiveCppSourceSet) sourceViewOf(this)).getObjectiveCpp().configure(action);
		}

		@Override
		public void objectiveCppSources(@SuppressWarnings("rawtypes") Closure closure) {
			objectiveCppSources(ConfigureUtil.configureUsing(closure));
		}

		@Override
		public NativeHeaderSet getPrivateHeaders() {
			return ((HasHeadersSourceSet) sourceViewOf(this)).getHeaders().get();
		}

		@Override
		public void privateHeaders(Action<? super NativeHeaderSet> action) {
			((HasHeadersSourceSet) sourceViewOf(this)).getHeaders().configure(action);
		}

		@Override
		public void privateHeaders(@SuppressWarnings("rawtypes") Closure closure) {
			privateHeaders(configureUsing(closure));
		}

		@Override
		public NativeHeaderSet getPublicHeaders() {
			return ((HasPublicSourceSet) sourceViewOf(this)).getPublic().get();
		}

		@Override
		public void publicHeaders(Action<? super NativeHeaderSet> action) {
			((HasPublicSourceSet) sourceViewOf(this)).getPublic().configure(action);
		}

		@Override
		public void publicHeaders(@SuppressWarnings("rawtypes") Closure closure) {
			publicHeaders(configureUsing(closure));
		}
	}
}
