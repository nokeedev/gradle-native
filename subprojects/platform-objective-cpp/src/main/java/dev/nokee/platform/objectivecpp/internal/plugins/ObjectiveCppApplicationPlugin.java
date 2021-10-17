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

import dev.nokee.language.base.internal.BaseLanguageSourceSetProjection;
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.cpp.CppHeaderSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppLanguageBasePlugin;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.ComponentSourcesPropertyRegistrationFactory;
import dev.nokee.platform.nativebase.HasHeadersSourceSet;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.NativeApplicationComponentModelRegistrationFactory;
import dev.nokee.platform.nativebase.internal.TargetBuildTypeRule;
import dev.nokee.platform.nativebase.internal.TargetMachineRule;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.objectivecpp.HasObjectiveCppSourceSet;
import dev.nokee.platform.objectivecpp.ObjectiveCppApplication;
import dev.nokee.platform.objectivecpp.ObjectiveCppApplicationSources;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelActions.executeUsingProjection;
import static dev.nokee.model.internal.core.ModelNodes.mutate;
import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.*;
import static dev.nokee.platform.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.*;
import static org.gradle.util.ConfigureUtil.configureUsing;

public class ObjectiveCppApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public ObjectiveCppApplicationPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		// Create the component
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(ObjectiveCppLanguageBasePlugin.class);
		val components = project.getExtensions().getByType(ComponentContainer.class);
		ModelNodeUtils.get(ModelNodes.of(components), NodeRegistrationFactoryRegistry.class).registerFactory(of(ObjectiveCppApplication.class), name -> objectiveCppApplication(name, project));
		val componentProvider = components.register("main", ObjectiveCppApplication.class, configureUsingProjection(DefaultNativeApplicationComponent.class, baseNameConvention(project.getName()).andThen(configureBuildVariants())));
		val extension = componentProvider.get();

		// Other configurations
		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(finalizeModelNodeOf(componentProvider));

		project.getExtensions().add(ObjectiveCppApplication.class, EXTENSION_NAME, extension);
	}

	public static NodeRegistration objectiveCppApplication(String name, Project project) {
		return new NativeApplicationComponentModelRegistrationFactory(ObjectiveCppApplication.class, DefaultObjectiveCppApplication.class, project, (entity, path) -> {
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
				.withComponent(path.child("headers"))
				.withComponent(IsLanguageSourceSet.tag())
				.withComponent(managed(of(CppHeaderSet.class)))
				.withComponent(managed(of(BaseLanguageSourceSetProjection.class)))
				.build());

			registry.register(project.getExtensions().getByType(ComponentSourcesPropertyRegistrationFactory.class).create(path.child("sources"), ObjectiveCppApplicationSources.class));
		}).create(name).action(allDirectDescendants(mutate(of(ObjectiveCppSourceSet.class)))
			.apply(executeUsingProjection(of(ObjectiveCppSourceSet.class), withConventionOf(maven(ComponentName.of(name)), defaultObjectiveCppGradle(ComponentName.of(name)))::accept)));
	}

	public static abstract class DefaultObjectiveCppApplication implements ObjectiveCppApplication {
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
	}
}
