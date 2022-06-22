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
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelElementProviderSourceComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelPathComponent;
import dev.nokee.model.internal.core.ModelPropertyTag;
import dev.nokee.model.internal.core.ModelPropertyTypeComponent;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.tags.ModelComponentTag;
import dev.nokee.model.internal.tags.ModelTags;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelTypes.set;

public abstract class ModelPropertiesCapabilityPlugin<T extends ExtensionAware & PluginAware> implements Plugin<T> {
	private final ObjectFactory objects;

	@Inject
	public ModelPropertiesCapabilityPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(T target) {
		val model = target.getExtensions().getByType(ModelConfigurer.class);
		model.configure(ModelActionWithInputs.of(ModelTags.referenceOf(ModelPropertyTag.class), this::execute));
		model.configure(ModelActionWithInputs.of(ModelTags.referenceOf(ModelPropertyTag.class), ModelComponentReference.of(ElementNameComponent.class), this::execute));
		model.configure(ModelActionWithInputs.of(ModelTags.referenceOf(ModelPropertyTag.class), ModelComponentReference.of(ParentComponent.class), this::execute));
		model.configure(ModelActionWithInputs.of(ModelTags.referenceOf(ModelPropertyTag.class), ModelComponentReference.of(ElementNameComponent.class), ModelComponentReference.of(ParentComponent.class),this::execute));
		model.configure(ModelActionWithInputs.of(ModelTags.referenceOf(ModelPropertyTag.class), ModelComponentReference.of(ModelPropertyTypeComponent.class), this::execute));
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

	private void execute(ModelNode entity, ModelComponentTag<ModelPropertyTag> tag, ModelPropertyTypeComponent propertyType) {
		if (propertyType.get().equals(set(of(File.class)))) {
			val property = objects.fileCollection();
			entity.addComponent(new GradlePropertyComponent(property));
			entity.addComponent(new ModelElementProviderSourceComponent(property.getElements()));
		} else if (propertyType.get().isSubtypeOf(Map.class)) {
			val property = objects.mapProperty(propertyType.get().getTypeVariables().get(0).getConcreteType(), propertyType.get().getTypeVariables().get(1).getConcreteType());
			entity.addComponent(new GradlePropertyComponent(property));
			entity.addComponent(new ModelElementProviderSourceComponent(property));
		} else if (propertyType.get().isSubtypeOf(List.class)) {
			val property = objects.listProperty(propertyType.get().getTypeVariables().get(0).getConcreteType());
			entity.addComponent(new GradlePropertyComponent(property));
			entity.addComponent(new ModelElementProviderSourceComponent(property));
		} else if (propertyType.get().isSubtypeOf(Set.class)) {
			val property = objects.setProperty(propertyType.get().getTypeVariables().get(0).getConcreteType());
			entity.addComponent(new GradlePropertyComponent(property));
			entity.addComponent(new ModelElementProviderSourceComponent(property));
		} else {
			val property = objects.property(propertyType.get().getConcreteType());
			entity.addComponent(new GradlePropertyComponent(property));
			entity.addComponent(new ModelElementProviderSourceComponent(property));
		}
	}
}
