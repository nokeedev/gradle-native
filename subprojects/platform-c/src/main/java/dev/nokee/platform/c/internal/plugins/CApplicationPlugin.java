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

import com.google.common.collect.ImmutableList;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.BaseLanguageSourceSetProjection;
import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.model.internal.BaseDomainObjectViewProjection;
import dev.nokee.model.internal.BaseNamedDomainObjectViewProjection;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.c.CApplication;
import dev.nokee.platform.c.CApplicationSources;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.TargetBuildTypeRule;
import dev.nokee.platform.nativebase.internal.TargetMachineRule;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelActions.executeUsingProjection;
import static dev.nokee.model.internal.core.ModelNodes.discover;
import static dev.nokee.model.internal.core.ModelNodes.mutate;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.maven;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.withConventionOf;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.*;

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
		val components = project.getExtensions().getByType(ComponentContainer.class);
		ModelNodeUtils.get(ModelNodes.of(components), NodeRegistrationFactoryRegistry.class).registerFactory(of(CApplication.class),
			name -> cApplication(name, project));
		val componentProvider = components.register("main", CApplication.class, configureUsingProjection(DefaultNativeApplicationComponent.class, baseNameConvention(project.getName()).andThen(configureBuildVariants())));
		val extension = componentProvider.get();

		// Other configurations
		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(finalizeModelNodeOf(componentProvider));

		project.getExtensions().add(CApplication.class, EXTENSION_NAME, extension);
	}

	public static NodeRegistration<CApplication> cApplication(String name, Project project) {
		return NodeRegistration.of(name, of(CApplication.class))
			// TODO: Should configure FileCollection on CApplication
			//   and link FileCollection to source sets
			.action(allDirectDescendants(mutate(of(LanguageSourceSet.class)))
				.apply(executeUsingProjection(of(LanguageSourceSet.class), withConventionOf(maven(ComponentName.of(name)))::accept)))
			.withProjection(createdUsing(of(DefaultNativeApplicationComponent.class), nativeApplicationProjection(name, project)))
			.action(self(discover()).apply(ModelActionWithInputs.of(of(ModelPath.class), (entity, path) -> {
				val registry = project.getExtensions().getByType(ModelRegistry.class);

				// TODO: Should be created using CSourceSetSpec
				val c = registry.register(ModelRegistration.builder()
					.withPath(path.child("c"))
					.withProjection(managed(of(CSourceSet.class)))
					.withProjection(managed(of(BaseLanguageSourceSetProjection.class)))
					.build());

				// TODO: Should be created using CHeaderSetSpec
				val headers = registry.register(ModelRegistration.builder()
					.withPath(path.child("headers"))
					.withProjection(managed(of(CHeaderSet.class)))
					.withProjection(managed(of(BaseLanguageSourceSetProjection.class)))
					.build());

				// TODO: Should be created as ModelProperty (readonly) with CApplicationSources projection
				registry.register(ModelRegistration.builder()
					.withPath(path.child("sources"))
					.withProjection(managed(of(CApplicationSources.class)))
					.withProjection(managed(of(BaseDomainObjectViewProjection.class)))
					.withProjection(managed(of(BaseNamedDomainObjectViewProjection.class)))
					.build());

				// TODO: Should be created as ModelProperty (readonly) pointing to c source set
				registry.register(ModelRegistration.builder()
					.withPath(path.child("sources").child("c"))
					.action(ModelActionWithInputs.of(ModelType.of(ModelPath.class), ModelType.of(ModelState.IsAtLeastRealized.class), (e, p, ignored) -> {
						if (p.equals(path.child("sources").child("c"))) {
							ModelStates.realize(ModelNodes.of(c));
						} else if (p.equals(path.child("c"))) {
							ModelStates.realize(ModelNodes.of(registry.get(path.child("sources").child("c").toString(), CSourceSet.class)));
						}
					}))
					.action(ModelActionWithInputs.of(ModelType.of(ModelPath.class), ModelType.of(ModelState.IsAtLeastCreated.class), (e, p, ignored) -> {
						if (p.equals(path.child("sources").child("c"))) {
							e.addComponent(new ModelProjection() {
								private final ModelNode delegate = ModelNodes.of(c);

								@Override
								public <T> boolean canBeViewedAs(ModelType<T> type) {
									return ModelNodeUtils.canBeViewedAs(delegate, type);
								}

								@Override
								public <T> T get(ModelType<T> type) {
									return ModelNodeUtils.get(delegate, type);
								}

								@Override
								public Iterable<String> getTypeDescriptions() {
									return ImmutableList.of(ModelNodeUtils.getTypeDescription(delegate).orElse("<unknown>"));
								}
							});
						}
					}))
					.build());


				// TODO: Should be created as ModelProperty (readonly) pointing to headers source set
				registry.register(ModelRegistration.builder()
					.withPath(path.child("sources").child("headers"))
					.action(ModelActionWithInputs.of(ModelType.of(ModelPath.class), ModelType.of(ModelState.IsAtLeastRealized.class), (e, p, ignored) -> {
						if (p.equals(path.child("sources").child("headers"))) {
							ModelStates.realize(ModelNodes.of(headers));
						} else if (p.equals(path.child("headers"))) {
							ModelStates.realize(ModelNodes.of(registry.get(path.child("sources").child("headers").toString(), CHeaderSet.class)));
						}
					}))
					.action(ModelActionWithInputs.of(ModelType.of(ModelPath.class), ModelType.of(ModelState.IsAtLeastCreated.class), (e, p, ignored) -> {
						if (p.equals(path.child("sources").child("headers"))) {
							e.addComponent(new ModelProjection() {
								private final ModelNode delegate = ModelNodes.of(headers);

								@Override
								public <T> boolean canBeViewedAs(ModelType<T> type) {
									return ModelNodeUtils.canBeViewedAs(delegate, type);
								}

								@Override
								public <T> T get(ModelType<T> type) {
									return ModelNodeUtils.get(delegate, type);
								}

								@Override
								public Iterable<String> getTypeDescriptions() {
									return ImmutableList.of(ModelNodeUtils.getTypeDescription(delegate).orElse("<unknown>"));
								}
							});
						}
					}))
					.build());
			})));
	}
}
