package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.utils.Cast;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public class BaseVariant {
	@Getter private final VariantIdentifier<?> identifier;
	@Getter private final DomainObjectSet<Binary> binaryCollection;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	@Getter private final Property<Binary> developmentBinary;

	protected BaseVariant(VariantIdentifier<?> identifier, ObjectFactory objects) {
		this.identifier = identifier;
		this.objects = objects;
		this.binaryCollection = objects.domainObjectSet(Binary.class);
		this.developmentBinary = objects.property(Binary.class);
	}

	public BuildVariantInternal getBuildVariant() {
		return (BuildVariantInternal) identifier.getBuildVariant();
	}

	public BinaryView<Binary> getBinaries() {
		return Cast.uncheckedCastBecauseOfTypeErasure(getObjects().newInstance(DefaultBinaryView.class, Binary.class, binaryCollection, Realizable.IDENTITY));
	}
}
