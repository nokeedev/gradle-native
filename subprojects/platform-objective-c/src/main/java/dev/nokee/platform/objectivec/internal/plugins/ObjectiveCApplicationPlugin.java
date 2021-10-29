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
package dev.nokee.platform.objectivec.internal.plugins;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.BaseLanguageSourceSetProjection;
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.c.internal.plugins.CHeaderSetRegistrationFactory;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCLanguageBasePlugin;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCSourceSetRegistrationFactory;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.nativebase.HasHeadersSourceSet;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.*;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.objectivec.HasObjectiveCSourceSet;
import dev.nokee.platform.objectivec.ObjectiveCApplication;
import dev.nokee.platform.objectivec.ObjectiveCApplicationSources;
import groovy.lang.Closure;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.*;
import static dev.nokee.platform.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.*;
import static org.gradle.util.ConfigureUtil.configureUsing;

public class ObjectiveCApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public ObjectiveCApplicationPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		// Create the component
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(ObjectiveCLanguageBasePlugin.class);
		val componentProvider = project.getExtensions().getByType(ModelRegistry.class).register(objectiveCApplication("main", project)).as(ObjectiveCApplication.class);
		componentProvider.configure(configureUsingProjection(DefaultNativeApplicationComponent.class, baseNameConvention(project.getName())));
		val extension = componentProvider.get();

		// Other configurations
		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(finalizeModelNodeOf(componentProvider));

		project.getExtensions().add(ObjectiveCApplication.class, EXTENSION_NAME, extension);
	}

	public static ModelRegistration objectiveCApplication(String name, Project project) {
		val identifier = ComponentIdentifier.builder().name(ComponentName.of(name)).displayName("Objective-C application").withProjectIdentifier(ProjectIdentifier.of(project)).build();
		return new NativeApplicationComponentModelRegistrationFactory(ObjectiveCApplication.class, DefaultObjectiveCApplication.class, project, (entity, path) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);

			registry.register(project.getExtensions().getByType(ObjectiveCSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(entity.getComponent(ComponentIdentifier.class), "objectiveC")));
			registry.register(project.getExtensions().getByType(CHeaderSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(entity.getComponent(ComponentIdentifier.class), "headers")));

			registry.register(project.getExtensions().getByType(ComponentSourcesPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "sources"), ObjectiveCApplicationSources.class));

			project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), ModelComponentReference.ofAny(ModelComponentType.projectionOf(ObjectiveCSourceSet.class)), (e, p, ignored, projection) -> {
				if (path.isDescendant(p)) {
					withConventionOf(maven(ComponentName.of(name)), defaultObjectiveCGradle(ComponentName.of(name))).accept(ModelNodeUtils.get(e, LanguageSourceSet.class));
				}
			}));
		}).create(identifier);
	}

	public static abstract class DefaultObjectiveCApplication implements ObjectiveCApplication
		, ModelBackedDependencyAwareComponentMixIn<NativeApplicationComponentDependencies>
		, ModelBackedVariantAwareComponentMixIn<NativeApplication>
		, ModelBackedSourceAwareComponentMixIn<ObjectiveCApplicationSources>
		, ModelBackedBinaryAwareComponentMixIn
		, ModelBackedTaskAwareComponentMixIn
		, ModelBackedHasDevelopmentVariantMixIn<NativeApplication>
		, ModelBackedTargetMachineAwareComponentMixIn
		, ModelBackedTargetBuildTypeAwareComponentMixIn
	{
		@Override
		public ObjectiveCSourceSet getObjectiveCSources() {
			return ((HasObjectiveCSourceSet) sourceViewOf(this)).getObjectiveC().get();
		}

		@Override
		public void objectiveCSources(Action<? super ObjectiveCSourceSet> action) {
			((HasObjectiveCSourceSet) sourceViewOf(this)).getObjectiveC().configure(action);
		}

		@Override
		public void objectiveCSources(@SuppressWarnings("rawtypes") Closure closure) {
			objectiveCSources(configureUsing(closure));
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
	}
}
