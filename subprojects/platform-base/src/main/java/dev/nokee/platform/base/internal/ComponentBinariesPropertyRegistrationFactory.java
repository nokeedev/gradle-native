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

import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.ModelPropertyTag;
import dev.nokee.model.internal.core.ModelPropertyTypeComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.internal.elements.ComponentElementTypeComponent;
import dev.nokee.platform.base.internal.elements.ComponentElementsTag;
import lombok.val;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toPath;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelTypes.map;

public final class ComponentBinariesPropertyRegistrationFactory {
	private final ProviderFactory providers;
	private final ModelLookup modelLookup;
	private final ObjectFactory objects;

	public ComponentBinariesPropertyRegistrationFactory(ProviderFactory providers, ModelLookup modelLookup, ObjectFactory objects) {
		this.providers = providers;
		this.modelLookup = modelLookup;
		this.objects = objects;
	}

	public ModelRegistration create(ModelPropertyIdentifier identifier) {
		val path = toPath(identifier);
		assert path.getParent().isPresent();
		val ownerPath = path.getParent().get();
		return ModelRegistration.builder()
			.withComponent(identifier)
			.withComponent(ModelPropertyTag.instance())
			.withComponent(ConfigurableTag.tag())
			.withComponent(ComponentElementsTag.tag())
			.withComponent(new ViewConfigurationBaseComponent(modelLookup.get(ownerPath)))
			.withComponent(new ComponentElementTypeComponent(of(Binary.class)))
			.withComponent(new ModelPropertyTypeComponent(map(of(String.class), of(Binary.class))))
			.withComponent(new GradlePropertyComponent(objects.mapProperty(String.class, Binary.class)))
			.withComponent(createdUsing(of(BinaryView.class), () -> new BinaryViewAdapter<>(new ViewAdapter<>(Binary.class, new ModelNodeBackedViewStrategy(providers, objects, () -> {
				ModelStates.realize(modelLookup.get(ownerPath));
				ModelStates.finalize(modelLookup.get(ownerPath));
			})))))
			.build();
	}
}
