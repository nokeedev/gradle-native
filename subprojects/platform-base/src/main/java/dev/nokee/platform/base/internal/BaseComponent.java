package dev.nokee.platform.base.internal;

import dev.nokee.utils.Cast;
import dev.nokee.language.base.internal.SourceSet;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.Variant;
import dev.nokee.runtime.base.internal.DimensionType;
import dev.nokee.utils.Cast;
import lombok.Getter;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;

import javax.inject.Inject;

public abstract class BaseComponent<T extends Variant> {
	@Getter private final NamingScheme names;
	@Getter private final VariantCollection<T> variantCollection;
	@Getter private final DomainObjectSet<Binary> binaryCollection;
	@Getter private final DomainObjectSet<SourceSet> sourceCollection;
	@Getter private final BinaryView<Binary> binaries;

	protected BaseComponent(NamingScheme names, Class<T> variantType) {
		this.names = names;
		this.variantCollection = Cast.uncheckedCastBecauseOfTypeErasure(getObjects().newInstance(VariantCollection.class, variantType));
		this.binaries = Cast.uncheckedCastBecauseOfTypeErasure(getObjects().newInstance(VariantAwareBinaryView.class, Binary.class, variantCollection.getAsView(variantType)));
		this.binaryCollection = getObjects().domainObjectSet(Binary.class);
		this.sourceCollection = getObjects().domainObjectSet(SourceSet.class);

		getDimensions().finalizeValueOnRead();
	}

	public String getName() {
		return "main";
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	// TODO: We may want to model this as a DimensionRegistry for more richness than a plain set
	public abstract SetProperty<DimensionType> getDimensions();

	// TODO: We may want to model this as a BuildVariantRegistry for more richness than a plain set
	public abstract SetProperty<BuildVariant> getBuildVariants();

	public abstract Property<T> getDevelopmentVariant();

	public abstract Property<String> getBaseName();
}
