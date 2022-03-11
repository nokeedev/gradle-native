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
package dev.nokee.model.internal.ancestors;

import com.google.common.collect.ImmutableSet;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;

import java.util.Optional;

import static dev.nokee.model.internal.core.ModelComponentType.componentOf;

public abstract class AncestryCapabilityPlugin<T extends ExtensionAware & PluginAware> implements Plugin<T> {
	@Override
	public void apply(T target) {
		target.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ParentComponent.class), AncestryCapabilityPlugin::calculateAncestorsFromParent));
	}

	private static void calculateAncestorsFromParent(ModelNode entity, ParentComponent parent) {
		val ancestors = ImmutableSet.<AncestorRef>builder();
		Optional<ParentComponent> parentComponent = Optional.of(parent);
		while (parentComponent.isPresent()) {
			ancestors.add(AncestorRef.of(parentComponent.get().get()));
			parentComponent = parentComponent.flatMap(it -> it.get().findComponent(componentOf(ParentComponent.class)));
		}

		entity.addComponent(new AncestorsComponent(new Ancestors(ancestors.build())));
	}
}
