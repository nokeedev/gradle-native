package dev.nokee.platform.base.internal;

import dev.nokee.internal.Cast;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.Variant;
import dev.nokee.runtime.base.internal.DimensionType;
import lombok.Getter;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.SetProperty;

import javax.inject.Inject;

public abstract class BaseComponent<T extends Variant> {
	@Getter private final NamingScheme names;
	@Getter private final VariantCollection<T> variantCollection;
	@Getter private final DomainObjectSet<Binary> binaryCollection;
	@Getter private final BinaryView<Binary> binaries;

	protected BaseComponent(NamingScheme names, Class<T> variantType) {
		this.names = names;
		this.variantCollection = Cast.uncheckedCast("of type erasure", getObjects().newInstance(VariantCollection.class, variantType, (VariantFactory<T>)this::createVariant));
		this.binaries = Cast.uncheckedCast("of type erasure", getObjects().newInstance(VariantAwareBinaryView.class, Binary.class, variantCollection.getAsView(variantType)));
		this.binaryCollection = getObjects().domainObjectSet(Binary.class);

		getDimensions().finalizeValueOnRead();
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	protected abstract T createVariant(String name, BuildVariant buildVariant);

	// TODO: We may want to model this as a DimensionRegistry for more richness than a plain set
	public abstract SetProperty<DimensionType> getDimensions();

	// TODO: We may want to model this as a BuildVariantRegistry for more richness than a plain set
	public abstract SetProperty<BuildVariant> getBuildVariants();
}
