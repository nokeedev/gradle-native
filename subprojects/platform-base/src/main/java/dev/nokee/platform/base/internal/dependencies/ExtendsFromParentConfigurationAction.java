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
package dev.nokee.platform.base.internal.dependencies;

import com.google.common.collect.Iterables;
import dev.nokee.model.internal.actions.ModelAction;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelPathComponent;
import dev.nokee.model.internal.core.ModelSpecs;
import dev.nokee.model.internal.registry.ModelLookup;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

import java.util.Optional;

import static dev.nokee.model.internal.core.ModelComponentType.componentOf;

public final class ExtendsFromParentConfigurationAction implements ModelAction {
	private final Project project;
	private final ModelPath path;

	public ExtendsFromParentConfigurationAction(Project project, ModelPath path) {
		this.project = project;
		this.path = path;
	}

	@Override
	public void execute(ModelNode entity) {
		val p = entity.get(ModelPathComponent.class);
		val configuration = ModelNodeUtils.get(entity, Configuration.class);
		val parentConfigurationResult = project.getExtensions().getByType(ModelLookup.class).query(ModelSpecs.of(ModelNodes.withPath(path.getParent().get().child(p.get().getName()))));
		Optional.ofNullable(Iterables.getOnlyElement(parentConfigurationResult.get(), null)).ifPresent(parentConfigurationEntity -> {
			val parentConfiguration = ModelNodeUtils.get(parentConfigurationEntity, Configuration.class);
			if (!parentConfiguration.getName().equals(configuration.getName())) {
				configuration.extendsFrom(parentConfiguration);
			}
		});
	}
}
