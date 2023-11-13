/*
 * Copyright 2023 the original author or authors.
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

package dev.nokee.platform.base.internal;

import dev.nokee.model.internal.core.ModelComponentType;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelSpec;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.tags.ModelTag;
import dev.nokee.model.internal.tags.ModelTags;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.Project;

public abstract class ModelObjectFactory<T> implements NamedDomainObjectFactory<T> {
	private final Project project;
	private final Class<? extends ModelTag> tag;

	public ModelObjectFactory(Project project, Class<? extends ModelTag> tag) {
		this.project = project;
		this.tag = tag;
	}

	@Override
	public T create(String name) {
		ModelNode entity = project.getExtensions().getByType(ModelLookup.class).query(withName(name, tag)).get().stream().findFirst().orElse(null);
		if (entity == null) {
			return doCreate(name);
		} else {
			return ModelNodeContext.of(entity).execute(__ -> {
				T result = doCreate(name);
				ModelNodeContext.injectCurrentModelNodeIfAllowed(result);
				return result;
			});
		}
	}

	protected abstract T doCreate(String name);

	private static ModelSpec withName(String name, Class<? extends ModelTag> typeTag) {
		return it -> it.hasComponent(ModelTags.typeOf(typeTag)) && it.findComponent(ModelComponentType.componentOf(FullyQualifiedNameComponent.class)).map(t -> t.get().toString().equals(name)).orElse(false);
	}
}
