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
package dev.nokee.platform.base.internal.dependencies;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.HasName;
import dev.nokee.model.internal.names.ElementName;
import lombok.EqualsAndHashCode;

import java.util.Iterator;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

@EqualsAndHashCode
public final class DependencyBucketIdentifier implements DomainObjectIdentifier {
	private final Identity identity;
	private final DomainObjectIdentifier ownerIdentifier;

	private DependencyBucketIdentifier(Identity identity, DomainObjectIdentifier ownerIdentifier) {
		checkArgument(ownerIdentifier != null, "Cannot construct a dependency identifier because the owner identifier is null.");
		this.identity = identity;
		this.ownerIdentifier = ownerIdentifier;
	}

	public ElementName getName() {
		return identity.getName();
	}

	public DomainObjectIdentifier getOwnerIdentifier() {
		return ownerIdentifier;
	}

	public static DependencyBucketIdentifier of(String name, DomainObjectIdentifier ownerIdentifier) {
		return new DependencyBucketIdentifier(new Identity(ElementName.of(name)), ownerIdentifier);
	}

	@Override
	public Iterator<Object> iterator() {
		return ImmutableList.builder().addAll(ownerIdentifier).add(identity).build().iterator();
	}

	@Override
	public String toString() {
		throw new UnsupportedOperationException("not expecting a call");
	}

	@EqualsAndHashCode
	public static final class Identity implements HasName {
		private final ElementName name;

		private Identity(ElementName name) {
			this.name = Objects.requireNonNull(name);
		}

		@Override
		public ElementName getName() {
			return name;
		}
	}
}
