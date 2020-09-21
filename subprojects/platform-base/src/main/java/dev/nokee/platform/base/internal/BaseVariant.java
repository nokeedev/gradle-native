package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public class BaseVariant {
	@Getter private final VariantIdentifier<?> identifier;
	private final DomainObjectSet<Binary> binaryCollection;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	@Getter private final Property<Binary> developmentBinary;
	private final BinaryView<Binary> binaryView;

	protected BaseVariant(VariantIdentifier<?> identifier, ObjectFactory objects, BinaryView<Binary> binaryView) {
		this.identifier = identifier;
		this.objects = objects;
		this.binaryCollection = objects.domainObjectSet(Binary.class);
		this.developmentBinary = objects.property(Binary.class);
		this.binaryView = binaryView;
	}

	public BuildVariantInternal getBuildVariant() {
		return (BuildVariantInternal) identifier.getBuildVariant();
	}

	public DomainObjectSet<Binary> getBinaryCollection() {
		return binaryCollection;
	}

	public BinaryView<Binary> getBinaries() {
		return binaryView;
	}
}
