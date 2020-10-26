package dev.nokee.platform.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetViewInternal;
import dev.nokee.platform.base.*;
import dev.nokee.runtime.base.internal.DimensionType;
import lombok.Getter;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;

public abstract class BaseComponent<T extends Variant> implements Component {
	@Getter private final ComponentIdentifier<?> identifier;

	// TODO: We may want to model this as a DimensionRegistry for more richness than a plain set
	@Getter private final SetProperty<DimensionType> dimensions;

	@Getter private final Property<String> baseName;

	protected BaseComponent(ComponentIdentifier<?> identifier, ObjectFactory objects) {
		this.identifier = identifier;
		this.dimensions = objects.setProperty(DimensionType.class);
		this.baseName = objects.property(String.class);

		getDimensions().finalizeValueOnRead();
	}

	public abstract Provider<T> getDevelopmentVariant();

	public abstract BinaryView<Binary> getBinaries();

	public abstract VariantView<T> getVariants();

	public abstract VariantCollection<T> getVariantCollection();

	public abstract LanguageSourceSetViewInternal<LanguageSourceSet> getSources();

	// TODO: We may want to model this as a BuildVariantRegistry for more richness than a plain set
	public abstract SetProperty<BuildVariantInternal> getBuildVariants();
}
