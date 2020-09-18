package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.utils.Cast;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Named;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

// CAUTION: Never rely on the name of the variant, it isn't exposed on the public type!
public class BaseVariant implements Named {
	@Getter private final VariantIdentifier<?> identifier;
	@Getter private final DomainObjectSet<Binary> binaryCollection;
	@Getter private final String name;
	@Getter private final BuildVariantInternal buildVariant;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	@Getter private final Property<Binary> developmentBinary;

	protected BaseVariant(VariantIdentifier<?> identifier, String name, BuildVariantInternal buildVariant, ObjectFactory objects) {
		this.identifier = identifier;
		this.name = name;
		this.buildVariant = buildVariant;
		this.objects = objects;
		this.binaryCollection = objects.domainObjectSet(Binary.class);
		this.developmentBinary = objects.property(Binary.class);
	}

	public BinaryView<Binary> getBinaries() {
		return Cast.uncheckedCastBecauseOfTypeErasure(getObjects().newInstance(DefaultBinaryView.class, Binary.class, binaryCollection, Realizable.IDENTITY));
	}
}
