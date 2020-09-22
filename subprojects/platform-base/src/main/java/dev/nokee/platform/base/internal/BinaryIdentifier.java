package dev.nokee.platform.base.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.platform.base.Binary;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

@ToString
@EqualsAndHashCode
public final class BinaryIdentifier<T extends Binary> implements DomainObjectIdentifierInternal {
	@Getter private final BinaryName name;
	@Getter private final Class<T> type;
	@Getter private final DomainObjectIdentifierInternal ownerIdentifier;

	public BinaryIdentifier(BinaryName name, Class<T> type, DomainObjectIdentifierInternal ownerIdentifier) {
		checkArgument(name != null, "Cannot construct a binary identifier because the task name is null.");
		checkArgument(type != null, "Cannot construct a binary identifier because the task type is null.");
		checkArgument(ownerIdentifier != null, "Cannot construct a task identifier because the owner identifier is null.");
		checkArgument(isValidOwner(ownerIdentifier), "Cannot construct a task identifier because the owner identifier is invalid, only ComponentIdentifier and VariantIdentifier are accepted.");
		this.name = name;
		this.type = type;
		this.ownerIdentifier = ownerIdentifier;
	}

	private static boolean isValidOwner(DomainObjectIdentifierInternal ownerIdentifier) {
		return ownerIdentifier instanceof ComponentIdentifier || ownerIdentifier instanceof VariantIdentifier;
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
		throw new UnsupportedOperationException();
	}
}
