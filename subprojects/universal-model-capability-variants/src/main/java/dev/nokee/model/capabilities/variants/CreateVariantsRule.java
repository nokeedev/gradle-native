/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.model.capabilities.variants;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.internal.buffers.ModelBufferComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import lombok.val;

import static dev.nokee.model.internal.tags.ModelTags.tag;

public final class CreateVariantsRule extends ModelActionWithInputs.ModelAction1<ModelBufferComponent<KnownVariantInformationElement>> {
	private final ModelRegistry registry;

	public CreateVariantsRule(ModelRegistry registry) {
		this.registry = registry;
	}

	@Override
	protected void execute(ModelNode entity, ModelBufferComponent<KnownVariantInformationElement> knownVariants) {
		val builder = ImmutableList.<ModelNode>builder();
		for (KnownVariantInformationElement variant : knownVariants) {
			val variantEntity = registry.instantiate(ModelRegistration.builder()
				.withComponent(new ParentComponent(entity))
				.withComponent(new ElementNameComponent(variant.getName()))
				.withComponent(tag(IsVariant.class))
				.withComponent(new VariantInformationComponent(variant))
				.build());
			builder.add(variantEntity);
		}
		entity.addComponent(new LinkedVariantsComponent(builder.build()));
	}
}
