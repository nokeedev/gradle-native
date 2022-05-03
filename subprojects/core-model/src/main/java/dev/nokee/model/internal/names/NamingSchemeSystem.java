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
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelProjection;

import java.util.function.Function;

public final class NamingSchemeSystem extends ModelActionWithInputs.ModelAction2<ModelProjection, ElementNameComponent> {
	private final Function<? super ElementName, ? extends NamingScheme> namingSchemeFactory;

	public NamingSchemeSystem(Class<?> projectionType, Function<? super ElementName, ? extends NamingScheme> namingSchemeFactory) {
		super(ModelComponentReference.ofProjection(projectionType), ModelComponentReference.of(ElementNameComponent.class));
		this.namingSchemeFactory = namingSchemeFactory;
	}

	@Override
	protected void execute(ModelNode entity, ModelProjection projection, ElementNameComponent elementName) {
		// Deduplication required because of the old component elements implementation
		if (!entity.has(NamingSchemeComponent.class)) {
			entity.addComponent(new NamingSchemeComponent(namingSchemeFactory.apply(elementName.get())));
		}
	}
}
