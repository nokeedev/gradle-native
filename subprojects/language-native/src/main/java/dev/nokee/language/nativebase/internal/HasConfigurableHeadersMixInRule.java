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
import java.util.concurrent.Callable;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.type.ModelTypes.set;
import static dev.nokee.utils.FileCollectionUtils.elementsOf;

public final class HasConfigurableHeadersMixInRule extends ModelActionWithInputs.ModelAction2<ModelComponentTag<HasConfigurableHeadersMixIn.Tag>, ModelComponentTag<IsLanguageSourceSet>> {
	private final ModelRegistry registry;
	private final SourceSetFactory sourceSetFactory;
	private final ObjectFactory objects;

	HasConfigurableHeadersMixInRule(ModelRegistry registry, SourceSetFactory sourceSetFactory, ObjectFactory objects) {
		super(ModelTags.referenceOf(HasConfigurableHeadersMixIn.Tag.class), ModelTags.referenceOf(IsLanguageSourceSet.class));
		this.registry = registry;
		this.sourceSetFactory = sourceSetFactory;
		this.objects = objects;
	}

	@Override
	protected void execute(ModelNode entity, ModelComponentTag<HasConfigurableHeadersMixIn.Tag> ignored1, ModelComponentTag<IsLanguageSourceSet> ignored2) {
		val element = registry.register(ModelRegistration.builder()
			.withComponent(new ElementNameComponent("headers"))
			.withComponent(new ParentComponent(entity))
			.withComponent(tag(ModelPropertyTag.class))
			.withComponent(new ModelPropertyTypeComponent(set(ModelType.of(File.class))))
			.withComponent(createdUsing(ModelType.of(ConfigurableSourceSet.class), () -> {
				val result = sourceSetFactory.sourceSet();
				result.from(ModelNodeContext.getCurrentModelNode().get(GradlePropertyComponent.class).get());
				return result;
			}))
			.build());
		entity.addComponent(new HasConfigurableHeadersPropertyComponent(ModelNodes.of(element)));
		entity.addComponent(new ProjectHeaderSearchPaths(objects.fileCollection().from((Callable<?>) () -> element.as(SourceSet.class).get().getSourceDirectories())));
	}

}
