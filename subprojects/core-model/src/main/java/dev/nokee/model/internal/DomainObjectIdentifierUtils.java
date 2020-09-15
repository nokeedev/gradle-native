package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public final class DomainObjectIdentifierUtils {
	private DomainObjectIdentifierUtils() {}

	public static boolean isDescendent(DomainObjectIdentifier self, DomainObjectIdentifier other) {
		if (self instanceof DomainObjectIdentifierInternal) {
			Optional<? extends DomainObjectIdentifierInternal> parentIdentifier = ((DomainObjectIdentifierInternal) self).getParentIdentifier();
			while (parentIdentifier.isPresent() && !parentIdentifier.get().equals(other)) {
				parentIdentifier = parentIdentifier.get().getParentIdentifier();
			}

			return parentIdentifier.isPresent();
		}
		return false;
	}

	public static Optional<? extends DomainObjectIdentifier> getParent(DomainObjectIdentifier self) {
		if (self instanceof DomainObjectIdentifierInternal) {
			return ((DomainObjectIdentifierInternal) self).getParentIdentifier();
		}
		return Optional.empty();
	}

	public static DomainObjectIdentifier named(String name) {
		return new NamedDomainObjectIdentifierImpl(name);
	}

	@EqualsAndHashCode
	private static class NamedDomainObjectIdentifierImpl implements NamedDomainObjectIdentifier, DomainObjectIdentifierInternal {
		@Getter private final String name;

		private NamedDomainObjectIdentifierImpl(String name) {
			this.name = requireNonNull(name);
		}

		@Override
		public Optional<? extends DomainObjectIdentifierInternal> getParentIdentifier() {
			return Optional.empty();
		}

		@Override
		public String getDisplayName() {
			return "identifier '" + name + "'";
		}

		@Override
		public String toString() {
			return "DomainObjectIdentifierUtils.named(" + name + ")";
		}
	}
}
