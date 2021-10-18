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

import dev.nokee.language.base.internal.BaseLanguageSourceSetProjection;
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.ComponentSpec;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.c.CLibrary;
import dev.nokee.platform.c.CLibrarySources;
import dev.nokee.platform.c.HasCSourceSet;
import dev.nokee.platform.nativebase.*;
import dev.nokee.platform.nativebase.internal.*;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
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
import static dev.nokee.platform.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.*;
import static org.gradle.util.ConfigureUtil.configureUsing;

public class CLibraryPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "library";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public CLibraryPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		// Create the component
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(CLanguageBasePlugin.class);
		project.getExtensions().getByType(ModelLookup.class).get(ModelPath.path("components")).getComponent(ModelComponentType.componentOf(NodeRegistrationFactories.class)).registerFactory(ModelType.of(CLibrarySpec.class), name -> cLibrary(name, project));
		val components = project.getExtensions().getByType(ComponentContainer.class);
		val componentElement = components.register("main", CLibrarySpec.class);
		val componentProvider = componentElement.as(CLibrary.class);
		componentElement.as(DefaultNativeLibraryComponent.class).configure(it ->
			baseNameConvention(project.getName()).andThen(configureBuildVariants()).accept(componentProvider.get(), it));
		val extension = componentProvider.get();

		// Other configurations
		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetLinkageRule.class, extension.getTargetLinkages(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(finalizeModelNodeOf(componentProvider));

		project.getExtensions().add(CLibrary.class, EXTENSION_NAME, extension);
	}

	public interface CLibrarySpec extends ComponentSpec {}

	public static NodeRegistration cLibrary(String name, Project project) {
		return new NativeLibraryComponentModelRegistrationFactory(CLibrary.class, DefaultCLibrary.class, project, (entity, path) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);

			// TODO: Should be created using CSourceSetSpec
			registry.register(ModelRegistration.builder()
				.withComponent(path.child("c"))
				.withComponent(IsLanguageSourceSet.tag())
				.withComponent(managed(of(CSourceSet.class)))
				.withComponent(managed(of(BaseLanguageSourceSetProjection.class)))
				.build());

			// TODO: Should be created using CHeaderSetSpec
			registry.register(ModelRegistration.builder()
				.withComponent(path.child("public"))
				.withComponent(IsLanguageSourceSet.tag())
				.withComponent(managed(of(CHeaderSet.class)))
				.withComponent(managed(of(BaseLanguageSourceSetProjection.class)))
				.build());

			// TODO: Should be created using CHeaderSetSpec
			registry.register(ModelRegistration.builder()
				.withComponent(path.child("headers"))
				.withComponent(IsLanguageSourceSet.tag())
				.withComponent(managed(of(CHeaderSet.class)))
				.withComponent(managed(of(BaseLanguageSourceSetProjection.class)))
				.build());

			registry.register(project.getExtensions().getByType(ComponentSourcesPropertyRegistrationFactory.class).create(path.child("sources"), CLibrarySources.class));
		}).create(name);
	}

	public static abstract class DefaultCLibrary implements CLibrary, ModelBackedDependencyAwareComponentMixIn<NativeLibraryComponentDependencies>, ModelBackedSourceAwareComponentMixIn<CLibrarySources>, ModelBackedVariantAwareComponentMixIn<NativeLibrary>, ModelBackedBinaryAwareComponentMixIn {
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
