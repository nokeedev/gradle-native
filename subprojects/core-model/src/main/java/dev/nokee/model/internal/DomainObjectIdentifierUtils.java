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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.HasName;
import dev.nokee.model.internal.core.ModelPath;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.util.Path;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DomainObjectIdentifierUtils {
	private DomainObjectIdentifierUtils() {}

	public static boolean isDescendent(DomainObjectIdentifier self, DomainObjectIdentifier other) {
		val childCandidate = ImmutableList.copyOf(self);
		val parentCandidate = ImmutableList.copyOf(other);
		if (parentCandidate.size() < childCandidate.size()) {
			for (int i = 0; i < parentCandidate.size(); ++i) {
				if (!parentCandidate.get(i).equals(childCandidate.get(i))) {
					return false;
				}
			}
			return true;
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

	public static ModelPath toPath(DomainObjectIdentifier identifier) {
		return ModelPath.path(Streams.stream(identifier).flatMap(it -> {
			if (it instanceof ProjectIdentifier) {
				return Stream.empty();
			} else if (it instanceof HasName) {
				return Stream.of(((HasName) it).getName().toString());
			} else {
				throw new UnsupportedOperationException();
			}
		}).collect(Collectors.toList()));
	}

	public static Path toGradlePath(DomainObjectIdentifier identifier) {
		return Path.path(Path.SEPARATOR + Streams.stream(identifier).flatMap(it -> {
			if (it instanceof ProjectIdentifier) {
				return Stream.empty();
			} else if (it instanceof HasName) {
				return Stream.of(((HasName) it).getName().toString()).filter(s -> !s.isEmpty());
			} else {
				throw new UnsupportedOperationException();
			}
		}).collect(Collectors.joining(Path.SEPARATOR)));
	}
}
