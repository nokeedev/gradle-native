package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import static dev.nokee.utils.ConfigureUtils.configureDisplayName;

public class BaseVariant {
	@Getter private final VariantIdentifier<?> identifier;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	@Getter private final Property<Binary> developmentBinary;
	@Getter private final BinaryView<Binary> binaries;

	protected BaseVariant(VariantIdentifier<?> identifier, ObjectFactory objects, BinaryViewFactory binaryViewFactory) {
		this.identifier = identifier;
		this.objects = objects;
		this.developmentBinary = configureDisplayName(objects.property(Binary.class), "developmentBinary");
		this.binaries = binaryViewFactory.create(identifier);
	}

	public BuildVariantInternal getBuildVariant() {
		return (BuildVariantInternal) identifier.getBuildVariant();
	}
}
