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
import org.gradle.api.model.ObjectFactory;

import java.io.File;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelTypes.set;

@AutoFactory
public final class SourcePropertyRegistrationAction extends ModelActionWithInputs.ModelAction3<IdentifierComponent, IsLanguageSourceSet, ModelState.IsAtLeastRegistered> {
	private final LanguageSourceSetIdentifier identifier;
	private final Factory<ConfigurableSourceSet> sourceSetFactory;
	private final ModelRegistry registry;
	private final ObjectFactory objects;

	public SourcePropertyRegistrationAction(LanguageSourceSetIdentifier identifier, Factory<ConfigurableSourceSet> sourceSetFactory, @Provided ModelRegistry registry, @Provided ObjectFactory objects) {
		this.identifier = identifier;
		this.sourceSetFactory = sourceSetFactory;
		this.registry = registry;
		this.objects = objects;
	}

	@Override
	protected void execute(ModelNode entity, IdentifierComponent identifier, IsLanguageSourceSet tag, ModelState.IsAtLeastRegistered isAtLeastRegistered) {
		if (identifier.get().equals(this.identifier)) {
			val propertyIdentifier = ModelPropertyIdentifier.of(identifier.get(), "source");
			val element = registry.register(ModelRegistration.builder()
				.withComponent(new IdentifierComponent(propertyIdentifier))
				.withComponent(ModelPropertyTag.instance())
				.withComponent(new ModelPropertyTypeComponent(set(ModelType.of(File.class))))
				.withComponent(new GradlePropertyComponent(objects.fileCollection()))
				.withComponent(createdUsing(ModelType.of(ConfigurableSourceSet.class), () -> {
					val result = sourceSetFactory.create();
					result.from(ModelNodeContext.getCurrentModelNode().get(GradlePropertyComponent.class).get());
					return result;
				}))
				.build());
			entity.addComponent(new SourcePropertyComponent(ModelNodes.of(element)));
			entity.addComponent(new SourceFiles(element.as(SourceSet.class).map(SourceSet::getAsFileTree)));
		}
	}
}