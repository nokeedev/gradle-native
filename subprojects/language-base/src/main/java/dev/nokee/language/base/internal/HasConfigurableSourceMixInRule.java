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
package dev.nokee.language.base.internal;

import dev.nokee.internal.Factory;
import dev.nokee.language.base.ConfigurableSourceSet;
import dev.nokee.language.base.SourceSet;
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

public final class HasConfigurableSourceMixInRule extends ModelActionWithInputs.ModelAction2<KnownDomainObject<HasConfigurableSourceMixIn>, IsLanguageSourceSet> {
	private final Factory<ConfigurableSourceSet> sourceSetFactory;
	private final ModelRegistry registry;

	public HasConfigurableSourceMixInRule(Factory<ConfigurableSourceSet> sourceSetFactory, ModelRegistry registry) {
		super(ModelComponentReference.ofProjection(HasConfigurableSourceMixIn.class).asKnownObject(), ModelComponentReference.of(IsLanguageSourceSet.class));
		this.sourceSetFactory = sourceSetFactory;
		this.registry = registry;
	}

	@Override
	protected void execute(ModelNode entity, KnownDomainObject<HasConfigurableSourceMixIn> knownObject, IsLanguageSourceSet ignored) {
		val propertyIdentifier = ModelPropertyIdentifier.of(knownObject.getIdentifier(), "source");
		val element = registry.register(ModelRegistration.builder()
			.withComponent(new IdentifierComponent(propertyIdentifier))
			.withComponent(ModelPropertyTag.instance())
			.withComponent(new ModelPropertyTypeComponent(set(ModelType.of(File.class))))
			.withComponent(createdUsing(ModelType.of(ConfigurableSourceSet.class), sourceSetFactory))
			.build());
		entity.addComponent(new SourceFiles(element.as(SourceSet.class).map(SourceSet::getAsFileTree)));
	}
}
