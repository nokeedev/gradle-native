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
package dev.nokee.language.base.internal;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.ModelPropertyTag;
import dev.nokee.model.internal.core.ModelPropertyTypeComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.internal.ModelNodeBackedViewStrategy;
import dev.nokee.platform.base.internal.ViewAdapter;
import dev.nokee.platform.base.internal.ViewConfigurationBaseComponent;
import dev.nokee.platform.base.internal.elements.ComponentElementTypeComponent;
import dev.nokee.platform.base.internal.elements.ComponentElementsTag;
import lombok.val;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;

import java.util.function.Function;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toPath;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelTypes.map;

public final class ComponentSourcesPropertyRegistrationFactory {
	private final ProviderFactory providers;
	private final ModelLookup modelLookup;

	public ComponentSourcesPropertyRegistrationFactory(ProviderFactory providers, ModelLookup modelLookup) {
		this.providers = providers;
		this.modelLookup = modelLookup;
	}

	public ModelRegistration create(ModelPropertyIdentifier identifier) {
		val path = toPath(identifier);
		assert path.getParent().isPresent();
		val ownerPath = path.getParent().get();
		return ModelRegistration.builder()
			.withComponent(identifier)
			.withComponent(ModelPropertyTag.instance())
			.withComponent(ConfigurableTag.tag())
			.withComponent(ComponentElementsTag.tag())
			.withComponent(new ViewConfigurationBaseComponent(modelLookup.get(ownerPath)))
			.withComponent(new ComponentElementTypeComponent(of(LanguageSourceSet.class)))
			.withComponent(new ModelPropertyTypeComponent(map(of(String.class), of(LanguageSourceSet.class))))
			.withComponent(createdUsing(of(SourceView.class), () -> new SourceViewAdapter<>(new ViewAdapter<>(LanguageSourceSet.class, new ModelNodeBackedViewStrategy(providers, () -> {
				ModelStates.realize(modelLookup.get(ownerPath));
				ModelStates.finalize(modelLookup.get(ownerPath));
			})))))
			.build();
	}

	public <T extends FunctionalSourceSet> ModelRegistration create(ModelPropertyIdentifier identifier, Class<T> sourceViewType, Function<? super ViewAdapter<LanguageSourceSet>, ? extends T> factory) {
		val path = toPath(identifier);
		assert path.getParent().isPresent();
		val ownerPath = path.getParent().get();
		return ModelRegistration.builder()
			.withComponent(identifier)
			.withComponent(ModelPropertyTag.instance())
			.withComponent(ConfigurableTag.tag())
			.withComponent(ComponentElementsTag.tag())
			.withComponent(new ViewConfigurationBaseComponent(modelLookup.get(ownerPath)))
			.withComponent(new ComponentElementTypeComponent(of(LanguageSourceSet.class)))
			.withComponent(new ModelPropertyTypeComponent(map(of(String.class), of(LanguageSourceSet.class))))
			.withComponent(createdUsing(of(sourceViewType), () -> factory.apply(new ViewAdapter<>(LanguageSourceSet.class, new ModelNodeBackedViewStrategy(providers, () -> {
				ModelStates.realize(modelLookup.get(ownerPath));
				ModelStates.finalize(modelLookup.get(ownerPath));
			})))))
			.build();
	}
}
