/*
 * Copyright 2020-2021 the original author or authors.
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

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.platform.base.*;
import dev.nokee.runtime.core.CoordinateSet;
import dev.nokee.runtime.core.CoordinateSpace;
import dev.nokee.utils.Cast;
import lombok.Getter;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;

public abstract class BaseComponent<T extends Variant> implements Component, ModelNodeAware {
	private final ModelNode node = ModelNodeContext.getCurrentModelNode();
	@Getter private final ComponentIdentifier identifier;

	// TODO: We may want to model this as a DimensionRegistry for more richness than a plain set
	private final ListProperty<CoordinateSet<?>> dimensions;
	private final Property<CoordinateSpace> finalSpace;

	@Getter private final Property<String> baseName;

	protected BaseComponent(ComponentIdentifier identifier, ObjectFactory objects) {
		this.identifier = identifier;
		this.dimensions = Cast.uncheckedCastBecauseOfTypeErasure(objects.listProperty(CoordinateSet.class));
		this.finalSpace = objects.property(CoordinateSpace.class);
		this.baseName = objects.property(String.class);

		getDimensions().finalizeValueOnRead();

		getFinalSpace().convention(getDimensions().map(CoordinateSpace::cartesianProduct));
		getFinalSpace().disallowChanges();
		getFinalSpace().finalizeValueOnRead();
	}

	public ListProperty<CoordinateSet<?>> getDimensions() {
		return dimensions;
	}

	public Property<CoordinateSpace> getFinalSpace() {
		return finalSpace;
	}

	public abstract Provider<T> getDevelopmentVariant();

	public abstract BinaryView<Binary> getBinaries();

	public abstract VariantView<T> getVariants();

	public abstract VariantCollection<T> getVariantCollection();

//	public abstract LanguageSourceSetViewInternal<LanguageSourceSet> getSources();

	// TODO: We may want to model this as a BuildVariantRegistry for more richness than a plain set
	public abstract SetProperty<BuildVariantInternal> getBuildVariants();

	@Override
	public ModelNode getNode() {
		return node;
	}
}
