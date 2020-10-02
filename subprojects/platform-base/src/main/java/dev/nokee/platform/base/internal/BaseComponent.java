package dev.nokee.platform.base.internal;

import dev.nokee.language.base.internal.SourceSet;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.Variant;
import dev.nokee.runtime.base.internal.DimensionType;
import lombok.Getter;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;

public abstract class BaseComponent<T extends Variant> {
	@Getter private final ComponentIdentifier<?> identifier;
	@Getter private final NamingScheme names;
	@Getter private final DomainObjectSet<Binary> binaryCollection;
	@Getter private final DomainObjectSet<SourceSet> sourceCollection;

	// TODO: We may want to model this as a DimensionRegistry for more richness than a plain set
	@Getter private final SetProperty<DimensionType> dimensions;

	@Getter private final Property<String> baseName;

	protected BaseComponent(ComponentIdentifier<?> identifier, NamingScheme names, ObjectFactory objects) {
		this.identifier = identifier;
		this.names = names;
		this.binaryCollection = objects.domainObjectSet(Binary.class);
		this.sourceCollection = objects.domainObjectSet(SourceSet.class);
		this.dimensions = objects.setProperty(DimensionType.class);
		this.baseName = objects.property(String.class);

		getDimensions().finalizeValueOnRead();
	}

	public abstract Provider<T> getDevelopmentVariant();

	public abstract BinaryView<Binary> getBinaries();

	public abstract VariantCollection<T> getVariantCollection();

	// TODO: We may want to model this as a BuildVariantRegistry for more richness than a plain set
	public abstract SetProperty<BuildVariantInternal> getBuildVariants();
}
