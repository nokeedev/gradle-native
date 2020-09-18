package dev.nokee.platform.base.internal;

import dev.nokee.language.base.internal.SourceSet;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.Variant;
import dev.nokee.runtime.base.internal.DimensionType;
import dev.nokee.utils.Cast;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;

public class BaseComponent<T extends Variant> {
	@Getter private final ComponentIdentifier<?> identifier;
	@Getter private final NamingScheme names;
	@Getter private final VariantCollection<T> variantCollection;
	@Getter private final DomainObjectSet<Binary> binaryCollection;
	@Getter private final DomainObjectSet<SourceSet> sourceCollection;
	@Getter private final BinaryView<Binary> binaries;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	// TODO: We may want to model this as a DimensionRegistry for more richness than a plain set
	@Getter private final SetProperty<DimensionType> dimensions;
	// TODO: We may want to model this as a BuildVariantRegistry for more richness than a plain set
	@Getter private final SetProperty<BuildVariantInternal> buildVariants;

	@Getter private final Property<T> developmentVariant;
	@Getter private final Property<String> baseName;

	protected BaseComponent(ComponentIdentifier<?> identifier, NamingScheme names, Class<T> variantType, ObjectFactory objects) {
		this.identifier = identifier;
		this.names = names;
		this.variantCollection = new VariantCollection<>(variantType, objects);
		this.binaries = Cast.uncheckedCastBecauseOfTypeErasure(objects.newInstance(VariantAwareBinaryView.class, new DefaultMappingView<Binary, T>(variantCollection.getAsView(variantType), Variant::getBinaries)));
		this.objects = objects;
		this.binaryCollection = objects.domainObjectSet(Binary.class);
		this.sourceCollection = objects.domainObjectSet(SourceSet.class);
		this.dimensions = objects.setProperty(DimensionType.class);
		this.buildVariants = objects.setProperty(BuildVariantInternal.class);
		this.developmentVariant = objects.property(variantType);
		this.baseName = objects.property(String.class);

		getDimensions().finalizeValueOnRead();
	}

	public String getName() {
		return "main";
	}
}
