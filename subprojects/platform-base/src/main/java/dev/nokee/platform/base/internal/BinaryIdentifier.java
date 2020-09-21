package dev.nokee.platform.base.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.platform.base.Binary;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Optional;

@ToString
@EqualsAndHashCode
public final class BinaryIdentifier<T extends Binary> implements DomainObjectIdentifierInternal {
	private final BinaryName name;
	private final Class<T> type;
	private final DomainObjectIdentifierInternal ownerIdentifier;

	public BinaryIdentifier(BinaryName name, Class<T> type, DomainObjectIdentifierInternal ownerIdentifier) {
		this.name = name;
		this.type = type;
		this.ownerIdentifier = ownerIdentifier;
	}

	public static <T extends Binary> BinaryIdentifier<T> of(BinaryName name, Class<T> type, DomainObjectIdentifier ownerIdentifier) {
		return new BinaryIdentifier<>(name, type, (DomainObjectIdentifierInternal)ownerIdentifier);
	}

	@Override
	public Optional<? extends DomainObjectIdentifierInternal> getParentIdentifier() {
		return Optional.of(ownerIdentifier);
	}

	@Override
	public String getDisplayName() {
		return null;
	}
}
