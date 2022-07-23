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
package dev.nokee.model.internal.names;

import dev.nokee.model.internal.actions.ModelActionSystem;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.core.ParentUtils;
import dev.nokee.model.internal.registry.ModelConfigurer;
import org.gradle.api.Plugin;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;

import static dev.nokee.model.internal.names.FullyQualifiedName.toFullyQualifiedName;
import static dev.nokee.model.internal.names.RelativeNames.toRelativeNames;

public abstract class NamesCapabilityPlugin<T extends ExtensionAware & PluginAware> implements Plugin<T> {
	@Override
	public void apply(T target) {
		target.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ParentComponent.class), ModelComponentReference.of(ElementNameComponent.class), ModelComponentReference.of(NamingSchemeComponent.class), NamesCapabilityPlugin::computeRelativelyQualifiedName));
		target.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ParentComponent.class), ModelComponentReference.of(ElementNameComponent.class), ModelComponentReference.of(NamingSchemeComponent.class), NamesCapabilityPlugin::computeFullyQualifiedName));
		target.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionSystem.updateSelectorForTag(RelativeNamesComponent.class));
		target.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionSystem.updateSelectorForTag(FullyQualifiedNameComponent.class));
		target.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionSystem.updateSelectorForTag(ElementNameComponent.class));
	}

	// ComponentFromEntity<MainComponentTag> read-only ancestors
	// ComponentFromEntity<ElementNameComponent> read-only ancestors
	private static void computeRelativelyQualifiedName(ModelNode entity, ParentComponent parent, ElementNameComponent elementName, NamingSchemeComponent namingScheme) {
		entity.addComponent(new RelativeNamesComponent(ParentUtils.stream(parent).collect(toRelativeNames(elementName.get()))));
	}

	// ComponentFromEntity<MainComponentTag> read-only ancestors
	// ComponentFromEntity<ElementNameComponent> read-only ancestors
	private static void computeFullyQualifiedName(ModelNode entity, ParentComponent parent, ElementNameComponent elementName, NamingSchemeComponent namingScheme) {
		entity.addComponent(new FullyQualifiedNameComponent(ParentUtils.stream(parent).collect(toFullyQualifiedName(elementName.get()))));
	}
}
