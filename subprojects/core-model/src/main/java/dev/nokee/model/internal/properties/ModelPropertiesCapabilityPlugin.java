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
package dev.nokee.model.internal.properties;

import dev.nokee.model.internal.core.DisplayName;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelPathComponent;
import dev.nokee.model.internal.core.ModelPropertyTag;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.tags.ModelComponentTag;
import dev.nokee.model.internal.tags.ModelTags;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class ModelPropertiesCapabilityPlugin<T extends ExtensionAware & PluginAware> implements Plugin<T> {
	@Override
	public void apply(T target) {
		val model = target.getExtensions().getByType(ModelConfigurer.class);
		model.configure(ModelActionWithInputs.of(ModelTags.referenceOf(ModelPropertyTag.class), this::execute));
		model.configure(ModelActionWithInputs.of(ModelTags.referenceOf(ModelPropertyTag.class), ModelComponentReference.of(ElementNameComponent.class), this::execute));
		model.configure(ModelActionWithInputs.of(ModelTags.referenceOf(ModelPropertyTag.class), ModelComponentReference.of(ParentComponent.class), this::execute));
		model.configure(ModelActionWithInputs.of(ModelTags.referenceOf(ModelPropertyTag.class), ModelComponentReference.of(ElementNameComponent.class), ModelComponentReference.of(ParentComponent.class),this::execute));
	}

	// ComponentFromEntity<ModelPathComponent> read-only (from ParentComponent)
	// ComponentFromEntity<DisplayNameComponent> read-only (from ParentComponent)
	private void execute(ModelNode entity, ModelComponentTag<ModelPropertyTag> tag, ElementNameComponent elementName, ParentComponent parent) {
		entity.addComponent(new DisplayNameComponent(propertyDisplayName(parent.get().find(DisplayNameComponent.class).map(it -> it.get()).orElse(defaultParentDisplayName(parent.get())), elementName.get())));
	}

	private void execute(ModelNode entity, ModelComponentTag<ModelPropertyTag> tag, ElementNameComponent elementName) {
		entity.addComponent(new DisplayNameComponent(propertyDisplayName(null, elementName.get())));
	}

	// ComponentFromEntity<ModelPathComponent> read-only (from ParentComponent)
	// ComponentFromEntity<DisplayNameComponent> read-only (from ParentComponent)
	private void execute(ModelNode entity, ModelComponentTag<ModelPropertyTag> tag, ParentComponent parent) {
		entity.addComponent(new DisplayNameComponent(propertyDisplayName(parent.get().find(DisplayNameComponent.class).map(it -> it.get()).orElse(defaultParentDisplayName(parent.get())), null)));
	}

	private void execute(ModelNode entity, ModelComponentTag<ModelPropertyTag> tag) {
		entity.addComponent(new DisplayNameComponent(propertyDefaultDisplayName()));
	}

	private static DisplayName defaultParentDisplayName(ModelNode parent) {
		return new DisplayName("entity" + parent.find(ModelPathComponent.class).map(it -> " " + quote(it.get().toString())).orElse(""));
	}

	private static DisplayName propertyDisplayName(@Nullable DisplayName parentDisplayName, @Nullable ElementName elementName) {
		return new DisplayName(Optional.ofNullable(parentDisplayName).map(it -> it + " ").orElse("") + "property" + Optional.ofNullable(elementName).map(it -> " " + quote(it.toString())).orElse(""));
	}

	private static DisplayName propertyDefaultDisplayName() {
		return new DisplayName("property");
	}

	private static String quote(String s) {
		return "'" + s + "'";
	}
}
