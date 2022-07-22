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
package dev.nokee.model.internal.names;

import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.tags.ModelTag;
import dev.nokee.model.internal.tags.ModelTags;

import java.util.function.Function;

public final class NamingSchemeSystem extends ModelActionWithInputs.ModelAction2<ModelComponent, ElementNameComponent> {
	private final Function<? super ElementName, ? extends NamingScheme> namingSchemeFactory;

	@SuppressWarnings("unchecked")
	public NamingSchemeSystem(Class<?> type, Function<? super ElementName, ? extends NamingScheme> namingSchemeFactory) {
		super((ModelComponentReference<ModelComponent>) forType(type), ModelComponentReference.of(ElementNameComponent.class));
		this.namingSchemeFactory = namingSchemeFactory;
	}

	@SuppressWarnings("unchecked")
	private static ModelComponentReference<? extends ModelComponent> forType(Class<?> type) {
		if (ModelTag.class.isAssignableFrom(type)) {
			return ModelTags.referenceOf((Class<ModelTag>) type);
		} else {
			return ModelComponentReference.ofProjection(type);
		}
	}

	@Override
	protected void execute(ModelNode entity, ModelComponent ignored, ElementNameComponent elementName) {
		// Deduplication required because of the old component elements implementation
		if (!entity.has(NamingSchemeComponent.class)) {
			entity.addComponent(new NamingSchemeComponent(namingSchemeFactory.apply(elementName.get())));
		}
	}
}
