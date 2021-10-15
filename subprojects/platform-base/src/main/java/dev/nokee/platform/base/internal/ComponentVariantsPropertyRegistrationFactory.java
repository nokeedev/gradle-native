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
package dev.nokee.platform.base.internal;

import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantView;
import lombok.val;
import org.gradle.api.provider.ProviderFactory;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelType.of;

public final class ComponentVariantsPropertyRegistrationFactory {
	private final ModelRegistry registry;
	private final ModelPropertyRegistrationFactory propertyFactory;
	private final ProviderFactory providerFactory;
	private final ModelLookup modelLookup;

	public ComponentVariantsPropertyRegistrationFactory(ModelRegistry registry, ModelPropertyRegistrationFactory propertyFactory, ProviderFactory providerFactory, ModelLookup modelLookup) {
		this.registry = registry;
		this.propertyFactory = propertyFactory;
		this.providerFactory = providerFactory;
		this.modelLookup = modelLookup;
	}

	// TODO: We should accept the property identifier and use that to figure out descendant, path, and finalizing.
	public ModelRegistration create(ModelPath path, Class<? extends Variant> elementType) {
		assert path.getParent().isPresent();
		val ownerPath = path.getParent().get();
		return ModelRegistration.builder()
			.withComponent(path)
			.withComponent(IsModelProperty.tag())
			.withComponent(createdUsing(of(VariantView.class), () -> new VariantViewAdapter<>(new ViewAdapter<>(elementType, new ModelNodeBackedViewStrategy(providerFactory, () -> ModelStates.finalize(modelLookup.get(ownerPath)))))))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(IsVariant.class), ModelComponentReference.ofAny(ModelComponentType.projectionOf(elementType)), (e, p, ignored, projection) -> {
				if (ownerPath.isDirectDescendant(p)) {
					registry.register(propertyFactory.create(path.child(p.getName()), e));
				}
			}))
			.build();
	}
}
