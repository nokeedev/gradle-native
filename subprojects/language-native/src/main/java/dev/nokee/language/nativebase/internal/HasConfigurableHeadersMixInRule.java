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
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.DomainObjectIdentifierUtils;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;

import java.io.File;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelTypes.set;
import static dev.nokee.utils.FileCollectionUtils.elementsOf;

public final class HasConfigurableHeadersMixInRule extends ModelActionWithInputs.ModelAction2<KnownDomainObject<HasConfigurableHeadersMixIn>, IsLanguageSourceSet> {
	private final ModelRegistry registry;
	private final SourceSetFactory sourceSetFactory;

	HasConfigurableHeadersMixInRule(ModelRegistry registry, SourceSetFactory sourceSetFactory) {
		super(ModelComponentReference.ofProjection(HasConfigurableHeadersMixIn.class).asKnownObject(), ModelComponentReference.of(IsLanguageSourceSet.class));
		this.registry = registry;
		this.sourceSetFactory = sourceSetFactory;
	}

	@Override
	protected void execute(ModelNode entity, KnownDomainObject<HasConfigurableHeadersMixIn> knownObject, IsLanguageSourceSet ignored) {
		val propertyIdentifier = ModelPropertyIdentifier.of(knownObject.getIdentifier(), "headers");
		val element = registry.register(ModelRegistration.builder()
			.withComponent(new IdentifierComponent(propertyIdentifier))
			.withComponent(ModelPropertyTag.instance())
			.withComponent(new ModelPropertyTypeComponent(set(ModelType.of(File.class))))
			.withComponent(createdUsing(ModelType.of(ConfigurableSourceSet.class), sourceSetFactory::sourceSet))
			.build());
		entity.addComponent(new ProjectHeaderSearchPaths(element.as(SourceSet.class).flatMap(elementsOf(SourceSet::getSourceDirectories))));
	}

}
