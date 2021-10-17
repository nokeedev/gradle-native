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
package dev.nokee.platform.cpp.internal.plugins;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.BaseLanguageSourceSetProjection;
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.cpp.CppHeaderSet;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.cpp.internal.plugins.CppLanguageBasePlugin;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.model.internal.BaseDomainObjectViewProjection;
import dev.nokee.model.internal.BaseNamedDomainObjectViewProjection;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.cpp.CppLibrary;
import dev.nokee.platform.cpp.CppLibrarySources;
import dev.nokee.platform.nativebase.internal.*;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

import java.util.stream.Collectors;

import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.*;

public class CppLibraryPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "library";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public CppLibraryPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		// Create the component
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(CppLanguageBasePlugin.class);
		val components = project.getExtensions().getByType(ComponentContainer.class);
		ModelNodeUtils.get(ModelNodes.of(components), NodeRegistrationFactoryRegistry.class).registerFactory(of(CppLibrary.class), name -> cppLibrary(name, project));
		val componentProvider = components.register("main", CppLibrary.class, configureUsingProjection(DefaultNativeLibraryComponent.class, baseNameConvention(project.getName()).andThen(configureBuildVariants())));
		val extension = componentProvider.get();

		// Other configurations
		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetLinkageRule.class, extension.getTargetLinkages(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(finalizeModelNodeOf(componentProvider));

		project.getExtensions().add(CppLibrary.class, EXTENSION_NAME, extension);
	}

	public static NodeRegistration cppLibrary(String name, Project project) {
		return new NativeLibraryComponentModelRegistrationFactory(CppLibrary.class, project, (entity, path) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val propertyFactory = project.getExtensions().getByType(ModelPropertyRegistrationFactory.class);

			// TODO: Should be created using CppSourceSetSpec
			registry.register(ModelRegistration.builder()
				.withComponent(path.child("cpp"))
				.withComponent(IsLanguageSourceSet.tag())
				.withComponent(managed(of(CppSourceSet.class)))
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

			// TODO: Should be created as ModelProperty (readonly) with CppApplicationSources projection
			registry.register(ModelRegistration.builder()
				.withComponent(path.child("sources"))
				.withComponent(IsModelProperty.tag())
				.withComponent(managed(of(CppLibrarySources.class)))
				.withComponent(managed(of(BaseDomainObjectViewProjection.class)))
				.withComponent(managed(of(BaseNamedDomainObjectViewProjection.class)))
				.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastRegistered.class), (ee, pp, ignored) -> {
					if (path.child("sources").equals(pp)) {
						project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class), ModelComponentReference.of(IsLanguageSourceSet.class), ModelComponentReference.ofAny(projectionOf(LanguageSourceSet.class)), (e, p, ignored1, ignored2, projection) -> {
							if (path.isDescendant(p)) {
								val elementName = StringUtils.uncapitalize(Streams.stream(Iterables.skip(p, Iterables.size(path)))
									.filter(it -> !it.isEmpty())
									.map(StringUtils::capitalize)
									.collect(Collectors.joining()));
								registry.register(propertyFactory.create(path.child("sources").child(elementName), e));
							}
						}));
					}
				}))
				.build());
		}).create(name);
	}
}
