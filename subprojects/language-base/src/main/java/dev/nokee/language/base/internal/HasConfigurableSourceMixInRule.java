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
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.core.ModelPropertyTag;
import dev.nokee.model.internal.core.ModelPropertyTypeComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.tags.ModelComponentTag;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.gradle.api.model.ObjectFactory;

import java.io.File;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.tags.ModelTags.typeOf;
import static dev.nokee.model.internal.type.ModelTypes.set;

public final class HasConfigurableSourceMixInRule extends ModelActionWithInputs.ModelAction2<ModelProjection, ModelComponentTag<IsLanguageSourceSet>> {
	private final Factory<ConfigurableSourceSet> sourceSetFactory;
	private final ModelRegistry registry;
	private final ObjectFactory objects;

	public HasConfigurableSourceMixInRule(Factory<ConfigurableSourceSet> sourceSetFactory, ModelRegistry registry, ObjectFactory objects) {
		super(ModelComponentReference.ofProjection(HasConfigurableSourceMixIn.class), ModelTags.referenceOf(IsLanguageSourceSet.class));
		this.sourceSetFactory = sourceSetFactory;
		this.registry = registry;
		this.objects = objects;
	}

	@Override
	protected void execute(ModelNode entity, ModelProjection knownObject, ModelComponentTag<IsLanguageSourceSet> ignored) {
		val element = registry.register(ModelRegistration.builder()
			.withComponent(new ElementNameComponent("source"))
			.withComponent(new ParentComponent(entity))
			.withComponent(tag(ModelPropertyTag.class))
			.withComponent(new ModelPropertyTypeComponent(set(ModelType.of(File.class))))
			.withComponent(createdUsing(ModelType.of(ConfigurableSourceSet.class), () -> {
				val result = sourceSetFactory.create();
				if (entity.hasComponent(typeOf(LegacySourceSetTag.class))) {
					result.convention(ModelNodeContext.getCurrentModelNode().get(GradlePropertyComponent.class).get());
				} else {
					result.from(ModelNodeContext.getCurrentModelNode().get(GradlePropertyComponent.class).get());
				}
				return result;
			}))
			.build());
		entity.addComponent(new SourcePropertyComponent(ModelNodes.of(element)));
		entity.addComponent(new SourceFiles(element.as(SourceSet.class).map(SourceSet::getAsFileTree)));
	}
}
