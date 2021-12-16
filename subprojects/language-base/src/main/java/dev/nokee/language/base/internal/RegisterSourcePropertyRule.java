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

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import dev.nokee.internal.Factory;
import dev.nokee.language.base.ConfigurableSourceSet;
import dev.nokee.language.base.SourceSet;
import dev.nokee.model.internal.DomainObjectIdentifierUtils;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;

@AutoFactory
public final class RegisterSourcePropertyRule extends ModelActionWithInputs.ModelAction3<Object, LanguageSourceSetIdentifier, ModelState.IsAtLeastRegistered> {
	private final Factory<ConfigurableSourceSet> sourceSetFactory;
	private final ModelRegistry registry;

	public RegisterSourcePropertyRule(Class<?> sourceSetTag, @Provided Factory<ConfigurableSourceSet> sourceSetFactory, @Provided ModelRegistry registry) {
		super(ModelComponentReference.of((Class<Object>) sourceSetTag), ModelComponentReference.of(LanguageSourceSetIdentifier.class), ModelComponentReference.of(ModelState.IsAtLeastRegistered.class));
		this.sourceSetFactory = sourceSetFactory;
		this.registry = registry;
	}

	@Override
	protected void execute(ModelNode entity, Object sourceSetTag, LanguageSourceSetIdentifier identifier, ModelState.IsAtLeastRegistered isAtLeastRegistered) {
		if (!entity.hasComponent(ModelComponentType.componentOf(SourceProperty.class))) {
			val propertyIdentifier = ModelPropertyIdentifier.of(identifier, "source");
			val element = registry.register(ModelRegistration.builder()
				.withComponent(DomainObjectIdentifierUtils.toPath(propertyIdentifier))
				.withComponent(propertyIdentifier)
				.withComponent(IsModelProperty.tag())
				.withComponent(createdUsing(ModelType.of(ConfigurableSourceSet.class), sourceSetFactory))
				.build());
			entity.addComponent(new SourceProperty(ModelNodes.of(element)));
			entity.addComponent(new SourceFiles(element.as(SourceSet.class).map(SourceSet::getAsFileTree)));
		}
	}
}
