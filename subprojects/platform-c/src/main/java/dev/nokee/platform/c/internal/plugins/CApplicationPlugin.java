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
package dev.nokee.platform.c.internal.plugins;

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.c.internal.plugins.CHeaderSetRegistrationFactory;
import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin;
import dev.nokee.language.c.internal.plugins.CSourceSetRegistrationFactory;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.ModelBackedBinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedDependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedHasAssembleTaskMixIn;
import dev.nokee.platform.base.internal.ModelBackedHasBaseNameLegacyMixIn;
import dev.nokee.platform.base.internal.ModelBackedHasDevelopmentVariantMixIn;
import dev.nokee.platform.base.internal.ModelBackedNamedMixIn;
import dev.nokee.platform.base.internal.ModelBackedSourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedTaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedVariantAwareComponentMixIn;
import dev.nokee.platform.c.CApplication;
import dev.nokee.platform.c.CApplicationSources;
import dev.nokee.platform.c.HasCSourceSet;
import dev.nokee.platform.nativebase.HasHeadersSourceSet;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetBuildTypeAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetLinkageAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetMachineAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.NativeApplicationComponentModelRegistrationFactory;
import dev.nokee.platform.nativebase.internal.dependencies.ModelBackedNativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import groovy.lang.Closure;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

import static dev.nokee.language.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.baseNameConvention;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.configureUsingProjection;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.finalizeModelNodeOf;
import static org.gradle.util.ConfigureUtil.configureUsing;

public class CApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public CApplicationPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		// Create the component
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(CLanguageBasePlugin.class);
		val componentProvider = project.getExtensions().getByType(ModelRegistry.class).register(cApplication("main", project)).as(CApplication.class);
		componentProvider.configure(configureUsingProjection(DefaultNativeApplicationComponent.class, baseNameConvention(project.getName())));
		val extension = componentProvider.get();

		// Other configurations
		project.afterEvaluate(finalizeModelNodeOf(componentProvider));

		project.getExtensions().add(CApplication.class, EXTENSION_NAME, extension);
	}

	public static ModelRegistration cApplication(String name, Project project) {
		val identifier = ComponentIdentifier.builder().name(ComponentName.of(name)).displayName("C application").withProjectIdentifier(ProjectIdentifier.of(project)).build();
		return new NativeApplicationComponentModelRegistrationFactory(CApplication.class, DefaultCApplication.class, project, (entity, path) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);

			registry.register(project.getExtensions().getByType(CSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(entity.get(IdentifierComponent.class).get(), "c"), true));
			registry.register(project.getExtensions().getByType(CHeaderSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(entity.get(IdentifierComponent.class).get(), "headers")));
		}).create(identifier);
	}

	public static abstract class DefaultCApplication implements CApplication
		, ModelBackedDependencyAwareComponentMixIn<NativeApplicationComponentDependencies, ModelBackedNativeApplicationComponentDependencies>
		, ModelBackedVariantAwareComponentMixIn<NativeApplication>
		, ModelBackedSourceAwareComponentMixIn<CApplicationSources, CApplicationSourcesAdapter>
		, ModelBackedBinaryAwareComponentMixIn
		, ModelBackedTaskAwareComponentMixIn
		, ModelBackedHasDevelopmentVariantMixIn<NativeApplication>
		, ModelBackedTargetMachineAwareComponentMixIn
		, ModelBackedTargetBuildTypeAwareComponentMixIn
		, ModelBackedTargetLinkageAwareComponentMixIn
		, ModelBackedHasBaseNameLegacyMixIn
		, ModelBackedNamedMixIn
		, ModelBackedHasAssembleTaskMixIn
	{
		@Override
		public CSourceSet getCSources() {
			return ((HasCSourceSet) sourceViewOf(this)).getC().get();
		}

		public CSourceSet getcSources() {
			return getCSources();
		}

		@Override
		public void cSources(Action<? super CSourceSet> action) {
			((HasCSourceSet) sourceViewOf(this)).getC().configure(action);
		}

		@Override
		public void cSources(@SuppressWarnings("rawtypes") Closure closure) {
			cSources(configureUsing(closure));
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
