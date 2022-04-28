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
import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.platform.base.Binary;
import lombok.EqualsAndHashCode;
import lombok.val;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toGradlePath;
import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
public final class BinaryIdentifier<T extends Binary> implements DomainObjectIdentifierInternal {
	private final BinaryIdentity identity;
	private final Class<T> type;
	private final DomainObjectIdentifier ownerIdentifier;

	public BinaryIdentifier(BinaryIdentity identity, Class<T> type, DomainObjectIdentifier ownerIdentifier) {
		requireNonNull(identity);
		checkArgument(ownerIdentifier != null, "Cannot construct a task identifier because the owner identifier is null.");
		checkArgument(isValidOwner(ownerIdentifier), "Cannot construct a task identifier because the owner identifier is invalid, only ComponentIdentifier and VariantIdentifier are accepted.");
		this.identity = identity;
		this.type = type;
		this.ownerIdentifier = ownerIdentifier;
	}

	public BinaryName getName() {
		return identity.getName();
	}

	public Class<T> getType() {
		return type;
	}

	public DomainObjectIdentifier getOwnerIdentifier() {
		return ownerIdentifier;
	}

	private static boolean isValidOwner(DomainObjectIdentifier ownerIdentifier) {
		return ownerIdentifier instanceof ComponentIdentifier || ownerIdentifier instanceof VariantIdentifier || ownerIdentifier instanceof ProjectIdentifier;
	}

	public static <T extends Binary> BinaryIdentifier<T> of(BinaryName name, Class<T> type, DomainObjectIdentifier ownerIdentifier) {
		checkArgument(type != null, "Cannot construct a binary identifier because the task type is null.");
		return new BinaryIdentifier<>(BinaryIdentity.of(name.toString(), "binary"), type, ownerIdentifier);
	}

	public static BinaryIdentifier<?> of(DomainObjectIdentifier ownerIdentifier, String name) {
		return new BinaryIdentifier<>(BinaryIdentity.of(name, "binary"), null, ownerIdentifier);
	}

	public static BinaryIdentifier<?> of(DomainObjectIdentifier ownerIdentifier, BinaryIdentity identity) {
		return new BinaryIdentifier<>(identity, null, ownerIdentifier);
	}

	public String getOutputDirectoryBase(String outputType) {
		val segments = new ArrayList<String>();

		segments.add(outputType);
		getComponentOwnerIdentifier()
			.map(ComponentIdentifier::getName)
			.map(ComponentName::get)
			.ifPresent(segments::add);
		getVariantOwnerIdentifier()
			.map(VariantIdentifier::getAmbiguousDimensions)
			.map(Dimensions::get)
			.filter(it -> !it.isEmpty())
			.ifPresent(segments::addAll);

		return String.join("/", segments);
	}

	private Optional<ComponentIdentifier> getComponentOwnerIdentifier() {
		if (ownerIdentifier instanceof VariantIdentifier) {
			return Optional.of(((VariantIdentifier<?>) ownerIdentifier).getComponentIdentifier());
		} else if (ownerIdentifier instanceof ComponentIdentifier) {
			return Optional.of((ComponentIdentifier) ownerIdentifier);
		}
		return Optional.empty();
	}

	private Optional<VariantIdentifier<?>> getVariantOwnerIdentifier() {
		if (ownerIdentifier instanceof VariantIdentifier) {
			return Optional.of((VariantIdentifier<?>) ownerIdentifier);
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
