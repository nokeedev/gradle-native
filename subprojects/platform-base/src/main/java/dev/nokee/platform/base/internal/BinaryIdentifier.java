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
package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.names.MainName;
import lombok.EqualsAndHashCode;
import lombok.val;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toGradlePath;
import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
public final class BinaryIdentifier implements DomainObjectIdentifier {
	private final BinaryIdentity identity;
	private final DomainObjectIdentifier ownerIdentifier;

	public BinaryIdentifier(BinaryIdentity identity, DomainObjectIdentifier ownerIdentifier) {
		requireNonNull(identity);
		checkArgument(ownerIdentifier != null, "Cannot construct a task identifier because the owner identifier is null.");
		this.identity = identity;
		this.ownerIdentifier = ownerIdentifier;
	}

	public ElementName getName() {
		return identity.getName();
	}

	public DomainObjectIdentifier getOwnerIdentifier() {
		return ownerIdentifier;
	}

	public static BinaryIdentifier of(String name, DomainObjectIdentifier ownerIdentifier) {
		return new BinaryIdentifier(BinaryIdentity.of(name, "binary"), ownerIdentifier);
	}

	public static BinaryIdentifier of(DomainObjectIdentifier ownerIdentifier, String name) {
		return new BinaryIdentifier(BinaryIdentity.of(name, "binary"), ownerIdentifier);
	}

	public static BinaryIdentifier of(DomainObjectIdentifier ownerIdentifier, BinaryIdentity identity) {
		return new BinaryIdentifier(identity, ownerIdentifier);
	}

	public String getOutputDirectoryBase(String outputType) {
		val segments = new ArrayList<String>();

		segments.add(outputType);
		getComponentOwnerIdentifier()
			.map(ModelObjectIdentifier::getName)
			.filter(it -> !(it instanceof MainName))
			.map(Object::toString)
			.ifPresent(segments::add);
		getVariantOwnerIdentifier()
			.map(VariantIdentifier::getAmbiguousDimensions)
			.map(Dimensions::get)
			.filter(it -> !it.isEmpty())
			.ifPresent(segments::addAll);

		return String.join("/", segments);
	}

	private Optional<ModelObjectIdentifier> getComponentOwnerIdentifier() {
		if (ownerIdentifier instanceof VariantIdentifier) {
			// TODO: Remove assumption that owner is ComponentIdentifier
			return Optional.of((ModelObjectIdentifier) ((VariantIdentifier) ownerIdentifier).getOwnerIdentifier());
		} else if (ownerIdentifier instanceof ModelObjectIdentifier) {
			return Optional.of((ModelObjectIdentifier) ownerIdentifier);
		}
		return Optional.empty();
	}

	private Optional<VariantIdentifier> getVariantOwnerIdentifier() {
		if (ownerIdentifier instanceof VariantIdentifier) {
			return Optional.of((VariantIdentifier) ownerIdentifier);
		}
		return Optional.empty();
	}

	@Override
	public String toString() {
		return identity.getDisplayName() + " '" + toGradlePath(this) + "'";
	}

	@Override
	public Iterator<Object> iterator() {
		return ImmutableList.builder().addAll(ownerIdentifier).add(identity).build().iterator();
	}
}
