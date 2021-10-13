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
package dev.nokee.platform.nativebase.internal.rules;

import com.google.common.collect.ImmutableMap;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationVariant;
import dev.nokee.platform.nativebase.internal.NativeApplicationComponentVariants;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import lombok.val;
import org.gradle.api.Project;

import java.util.function.Consumer;

import static dev.nokee.platform.nativebase.internal.plugins.NativeApplicationPlugin.nativeApplicationVariant;

public class CalculateNativeApplicationVariantAction extends ModelActionWithInputs.ModelAction1<ModelPath> {
	private final Project project;

	public CalculateNativeApplicationVariantAction(Project project) {
		this.project = project;
	}

	@Override
	protected void execute(ModelNode entity, ModelPath path) {
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		val propertyFactory = project.getExtensions().getByType(ModelPropertyRegistrationFactory.class);

		val component = ModelNodeUtils.get(entity, ModelType.of(DefaultNativeApplicationComponent.class));
		component.finalizeExtension(null);
		component.getDevelopmentVariant().set(project.getProviders().provider(new BuildableDevelopmentVariantConvention<>(() -> component.getVariants().get()))); // TODO: VariantView#get should force finalize the component.

		val variants = ImmutableMap.<BuildVariant, ModelNode>builder();
		component.getBuildVariants().get().forEach(new Consumer<BuildVariantInternal>() {
			@Override
			public void accept(BuildVariantInternal buildVariant) {
				val variantIdentifier = VariantIdentifier.builder().withBuildVariant(buildVariant).withComponentIdentifier(component.getIdentifier()).withType(DefaultNativeApplicationVariant.class).build();
				val variant = ModelNodeUtils.register(entity, nativeApplicationVariant(variantIdentifier, component, project));

				variants.put(buildVariant, ModelNodes.of(variant));
				onEachVariantDependencies(variant.as(NativeApplication.class), ModelNodes.of(variant).getComponent(ModelComponentType.componentOf(VariantComponentDependencies.class)));

				registry.register(propertyFactory.create(path.child("variants").child(variantIdentifier.getUnambiguousName()), ModelNodes.of(variant)));
			}

			private void onEachVariantDependencies(DomainObjectProvider<NativeApplication> variant, VariantComponentDependencies<?> dependencies) {
				dependencies.getOutgoing().getExportedBinary().convention(variant.flatMap(it -> it.getDevelopmentBinary()));
			}
		});
		entity.addComponent(new NativeApplicationComponentVariants(variants.build()));
	}
}
