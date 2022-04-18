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
package dev.nokee.language.nativebase.internal;

import dev.nokee.language.base.ConfigurableSourceSet;
import dev.nokee.language.base.SourceSet;
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.base.internal.SourceSetFactory;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.core.ModelPropertyTag;
import dev.nokee.model.internal.core.ModelPropertyTypeComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;

import java.io.File;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelTypes.set;
import static dev.nokee.utils.FileCollectionUtils.elementsOf;

public final class HasConfigurableHeadersMixInRule extends ModelActionWithInputs.ModelAction3<ModelProjection, IdentifierComponent, IsLanguageSourceSet> {
	private final ModelRegistry registry;
	private final SourceSetFactory sourceSetFactory;

	HasConfigurableHeadersMixInRule(ModelRegistry registry, SourceSetFactory sourceSetFactory) {
		super(ModelComponentReference.ofProjection(HasConfigurableHeadersMixIn.class), ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(IsLanguageSourceSet.class));
		this.registry = registry;
		this.sourceSetFactory = sourceSetFactory;
	}

	@Override
	protected void execute(ModelNode entity, ModelProjection knownObject, IdentifierComponent identifier, IsLanguageSourceSet ignored) {
		val propertyIdentifier = ModelPropertyIdentifier.of(identifier.get(), "headers");
		val element = registry.register(ModelRegistration.builder()
			.withComponent(new IdentifierComponent(propertyIdentifier))
			.withComponent(ModelPropertyTag.instance())
			.withComponent(new ModelPropertyTypeComponent(set(ModelType.of(File.class))))
			.withComponent(createdUsing(ModelType.of(ConfigurableSourceSet.class), sourceSetFactory::sourceSet))
			.build());
		entity.addComponent(new ProjectHeaderSearchPaths(element.as(SourceSet.class).flatMap(elementsOf(SourceSet::getSourceDirectories))));
	}

}
