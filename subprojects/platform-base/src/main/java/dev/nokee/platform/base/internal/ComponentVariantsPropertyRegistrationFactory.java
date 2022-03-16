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
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.type.TypeOf;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.elements.ComponentElementsPropertyRegistrationFactory;
import lombok.val;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toPath;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelType.of;

public final class ComponentVariantsPropertyRegistrationFactory {
	private final ComponentElementsPropertyRegistrationFactory factory = new ComponentElementsPropertyRegistrationFactory();
	private final ModelLookup modelLookup;

	public ComponentVariantsPropertyRegistrationFactory(ModelLookup modelLookup) {
		this.modelLookup = modelLookup;
	}

	public ModelRegistration create(ModelPropertyIdentifier identifier, Class<? extends Variant> elementType) {
		val path = toPath(identifier);
		assert path.getParent().isPresent();
		val ownerPath = path.getParent().get();
		return ModelRegistration.builder()
			.withComponent(identifier)
			.mergeFrom(factory.newProperty().baseRef(modelLookup.get(ownerPath)).elementType(of(elementType)).build())
			.withComponent(createdUsing(of(VariantView.class), () -> new VariantViewAdapter<>(ModelNodeUtils.get(ModelNodeContext.getCurrentModelNode(), of(new TypeOf<ViewAdapter<? extends Variant>>() {})))))
			.build();
	}
}
