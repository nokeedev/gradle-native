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

import dev.nokee.model.internal.actions.ModelAction;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.core.ParentUtils;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.platform.base.internal.dependencybuckets.LinkedConfiguration;
import lombok.val;
import org.gradle.api.artifacts.Configuration;

public final class ExtendsFromParentConfigurationAction implements ModelAction {
	@Override
	public void execute(ModelNode entity) {
		val parent = entity.get(ParentComponent.class);
		val name = entity.get(ElementNameComponent.class).get();
		val configuration = ModelNodeUtils.get(entity, Configuration.class);
		ParentUtils.stream(parent)
			.flatMap(it -> it.getComponents()
				.filter(LinkedConfiguration.class::isInstance)
				.map(component -> ((LinkedConfiguration) component).get())
				.filter(bucketEntity -> bucketEntity
					.find(ElementNameComponent.class)
					.map(nameComponent -> nameComponent.get().equals(name))
					.orElse(false)))
			.map(bucketEntity -> ModelNodeUtils.get(bucketEntity, Configuration.class))
			.filter(parentConfiguration -> !parentConfiguration.getName().equals(configuration.getName()))
			.findFirst()
			.ifPresent(configuration::extendsFrom);
	}
}
