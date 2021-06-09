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
	@Getter private final ComponentIdentifier<?> identifier;

	// TODO: We may want to model this as a DimensionRegistry for more richness than a plain set
	@Getter private final ListProperty<CoordinateSet<?>> dimensions;
	@Getter private final Property<CoordinateSpace> finalSpace;

	@Getter private final Property<String> baseName;

	protected BaseComponent(ComponentIdentifier<?> identifier, ObjectFactory objects) {
		this.identifier = identifier;
		this.dimensions = Cast.uncheckedCastBecauseOfTypeErasure(objects.listProperty(CoordinateSet.class));
		this.finalSpace = objects.property(CoordinateSpace.class);
		this.baseName = objects.property(String.class);

		getDimensions().finalizeValueOnRead();

		getFinalSpace().convention(getDimensions().map(CoordinateSpace::cartesianProduct));
		getFinalSpace().disallowChanges();
		getFinalSpace().finalizeValueOnRead();
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
