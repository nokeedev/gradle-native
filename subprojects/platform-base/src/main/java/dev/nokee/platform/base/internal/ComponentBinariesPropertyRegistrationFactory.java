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
package dev.nokee.platform.base.internal;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.provider.ProviderFactory;

import java.util.stream.Collectors;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toPath;
import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelType.of;

public final class ComponentBinariesPropertyRegistrationFactory {
	private final ModelRegistry registry;
	private final ModelPropertyRegistrationFactory propertyFactory;
	private final ModelConfigurer modelConfigurer;
	private final ProviderFactory providers;
	private final ModelLookup modelLookup;

	public ComponentBinariesPropertyRegistrationFactory(ModelRegistry registry, ModelPropertyRegistrationFactory propertyFactory, ModelConfigurer modelConfigurer, ProviderFactory providers, ModelLookup modelLookup) {
		this.registry = registry;
		this.propertyFactory = propertyFactory;
		this.modelConfigurer = modelConfigurer;
		this.providers = providers;
		this.modelLookup = modelLookup;
	}

	public ModelRegistration create(ModelPropertyIdentifier identifier) {
		val path = toPath(identifier);
		assert path.getParent().isPresent();
		val ownerPath = path.getParent().get();
		return ModelRegistration.builder()
			.withComponent(path)
			.withComponent(identifier)
			.withComponent(IsModelProperty.tag())
			.withComponent(createdUsing(of(BinaryView.class), () -> new BinaryViewAdapter<>(new ViewAdapter<>(Binary.class, new ModelNodeBackedViewStrategy(providers, () -> {
				ModelStates.realize(modelLookup.get(ownerPath));
				ModelStates.finalize(modelLookup.get(ownerPath));
			})))))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPropertyIdentifier.class), ModelComponentReference.of(ModelState.IsAtLeastRegistered.class), (ee, id, ignored) -> {
				if (id.equals(identifier)) {
					modelConfigurer.configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class), ModelComponentReference.of(IsBinary.class), ModelComponentReference.ofAny(projectionOf(Binary.class)), (e, p, ignored1, ignored2, projection) -> {
						if (ownerPath.isDescendant(p)) {
							val elementName = StringUtils.uncapitalize(Streams.stream(Iterables.skip(p, Iterables.size(ownerPath)))
								.filter(it -> !it.isEmpty())
								.map(StringUtils::capitalize)
								.collect(Collectors.joining()));
							registry.register(propertyFactory.create(ModelPropertyIdentifier.of(identifier, elementName), e));
						}
					}));
				}
			}))
			.build();
	}
}
