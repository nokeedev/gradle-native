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

package dev.nokee.platform.base.internal.plugins;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelElementConfigurableProviderSourceComponent;
import dev.nokee.model.internal.core.ModelElementProviderSourceComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.tags.ModelComponentTag;
import dev.nokee.model.internal.tags.ModelTag;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.internal.MainProjectionComponent;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.PolymorphicDomainObjectContainer;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.ModelProjections.createdUsingNoInject;
import static dev.nokee.utils.NamedDomainObjectCollectionUtils.registerIfAbsent;

@SuppressWarnings("unchecked")
public final class DomainObjectRegistration<T> extends ModelActionWithInputs.ModelAction3<MainProjectionComponent, ModelComponentTag<ModelTag>, FullyQualifiedNameComponent> {
	private final PolymorphicDomainObjectContainer<? super T> container;

	public DomainObjectRegistration(Class<? extends ModelTag> tag, PolymorphicDomainObjectContainer<? super T> container) {
		super(ModelComponentReference.of(MainProjectionComponent.class), ModelTags.referenceOf((Class<ModelTag>) tag), ModelComponentReference.of(FullyQualifiedNameComponent.class));
		this.container = container;
	}

	@Override
	protected void execute(ModelNode entity, MainProjectionComponent mainProjection, ModelComponentTag<ModelTag> ignored, FullyQualifiedNameComponent name) {
		final Class<T> elementType = (Class<T>) mainProjection.getProjectionType();
		final NamedDomainObjectProvider<T> elementProvider = registerIfAbsent(container, name.get().toString(), elementType);
		entity.addComponent(new ModelElementProviderSourceComponent(elementProvider));
		entity.addComponent(createdUsingNoInject(ModelType.of(elementType), elementProvider::get));
		entity.addComponent(createdUsing((ModelType<NamedDomainObjectProvider<T>>) ModelType.of(objectProviderFor(elementType).getType()), () -> elementProvider));
		entity.addComponent(new ModelElementConfigurableProviderSourceComponent(elementProvider));
	}

	private static <T> TypeToken<NamedDomainObjectProvider<T>> objectProviderFor(Class<T> type) {
		return new TypeToken<NamedDomainObjectProvider<T>>() {}.where(new TypeParameter<T>() {}, type);
	}
}
