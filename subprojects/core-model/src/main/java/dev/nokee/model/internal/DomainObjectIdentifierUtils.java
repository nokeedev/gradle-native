/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import lombok.EqualsAndHashCode;
import lombok.val;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

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

	public static Predicate<DomainObjectIdentifier> descendentOf(DomainObjectIdentifier owner) {
		return new DescendentIdentifierPredicate(owner);
	}

	@EqualsAndHashCode
	private static final class DescendentIdentifierPredicate implements Predicate<DomainObjectIdentifier> {
		private final DomainObjectIdentifier owner;

		DescendentIdentifierPredicate(DomainObjectIdentifier owner) {
			this.owner = owner;
		}

		@Override
		public boolean test(DomainObjectIdentifier identifier) {
			return isDescendent(identifier, owner);
		}

		@Override
		public String toString() {
			return "DomainObjectIdentifierUtils.descendentOf(" + owner + ")";
		}
	}

	public static Optional<? extends DomainObjectIdentifier> getParent(DomainObjectIdentifier self) {
		if (self instanceof DomainObjectIdentifierInternal) {
			return ((DomainObjectIdentifierInternal) self).getParentIdentifier();
		}
		return Optional.empty();
	}

	public static Predicate<DomainObjectIdentifier> directlyOwnedBy(DomainObjectIdentifier owner) {
		return new DirectlyOwnedIdentifierPredicate(owner);
	}

	@EqualsAndHashCode
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

	public static Predicate<DomainObjectIdentifier> withType(Class<?> type) {
		return new WithTypeIdentifierPredicate(type);
	}

	@EqualsAndHashCode
	private static final class WithTypeIdentifierPredicate implements Predicate<DomainObjectIdentifier> {
		private final Class<?> type;

		WithTypeIdentifierPredicate(Class<?> type) {
			this.type = type;
		}

		@Override
		public boolean test(DomainObjectIdentifier identifier) {
			if (identifier instanceof TypeAwareDomainObjectIdentifier) {
				return type.isAssignableFrom(((TypeAwareDomainObjectIdentifier<?>) identifier).getType());
			}
			return false;
		}

		@Override
		public String toString() {
			return "DomainObjectIdentifierUtils.withType(" + type.getCanonicalName() + ")";
		}
	}

	public static Predicate<DomainObjectIdentifier> named(String name) {
		return new NamedIdentifierPredicate(name);
	}

	@EqualsAndHashCode
	private static final class NamedIdentifierPredicate implements Predicate<DomainObjectIdentifier> {
		private final String name;

		private NamedIdentifierPredicate(String name) {
			this.name = name;
		}

		@Override
		public boolean test(DomainObjectIdentifier identifier) {
			if (identifier instanceof NameAwareDomainObjectIdentifier) {
				return Objects.equals(((NameAwareDomainObjectIdentifier) identifier).getName().toString(), name);
			}
			return false;
		}

		@Override
		public String toString() {
			return "DomainObjectIdentifierUtils.named(" + name + ")";
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

	@SuppressWarnings("unchecked")
	public static <S> TypeAwareDomainObjectIdentifier<S> uncheckedIdentifierCast(TypeAwareDomainObjectIdentifier<?> identifier) {
		return (TypeAwareDomainObjectIdentifier<S>) identifier;
	}

	@SuppressWarnings("unchecked")
	public static <S> TypeAwareDomainObjectIdentifier<S> castIdentifier(Class<S> outputIdentifierType, TypeAwareDomainObjectIdentifier<?> identifier) {
		if (outputIdentifierType.isAssignableFrom(identifier.getType())) {
			return (TypeAwareDomainObjectIdentifier<S>) identifier;
		}
		throw new ClassCastException(String.format("Failed to cast identifier %s of type %s to identifier of type %s.", identifier.toString(), identifier.getType().getName(), outputIdentifierType.getName()));
	}
}
