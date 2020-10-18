package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

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

	public static Predicate<? extends DomainObjectIdentifier> directlyOwnedBy(DomainObjectIdentifier owner) {
		return new DirectlyOwnedIdentifierPredicate(owner);
	}

	private static final class DirectlyOwnedIdentifierPredicate implements Predicate<DomainObjectIdentifier> {
		private final DomainObjectIdentifier owner;

		DirectlyOwnedIdentifierPredicate(DomainObjectIdentifier owner) {
			this.owner = owner;
		}

		@Override
		public boolean test(DomainObjectIdentifier identifier) {
			if (identifier instanceof DomainObjectIdentifierInternal) {
				val identifierInternal = (DomainObjectIdentifierInternal) identifier;
				return identifierInternal.getParentIdentifier().isPresent() && identifierInternal.getParentIdentifier().get().equals(owner);
			}
			return false;
		}

		@Override
		public String toString() {
			return "DomainObjectIdentifierUtils.directlyOwnedBy(" + owner + ")";
		}
	}

	public static Supplier<String> mapDisplayName(DomainObjectIdentifierInternal identifier) {
		return new MapDisplayName(identifier);
	}

	@EqualsAndHashCode
	private static class MapDisplayName implements Supplier<String> {
		private final DomainObjectIdentifierInternal identifier;

		public MapDisplayName(DomainObjectIdentifierInternal identifier) {
			this.identifier = identifier;
		}

		@Override
		public String get() {
			return identifier.getDisplayName();
		}

		@Override
		public String toString() {
			return "DomainObjectIdentifierUtils.mapDisplayName(" + identifier + ")";
		}
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
