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

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier;
import dev.nokee.platform.base.Binary;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;
import org.gradle.util.Path;

import java.util.ArrayList;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

@EqualsAndHashCode
public final class BinaryIdentifier<T extends Binary> implements DomainObjectIdentifierInternal, TypeAwareDomainObjectIdentifier<T> {
	@Getter private final BinaryName name;
	@Getter private final Class<T> type;
	@Getter private final DomainObjectIdentifier ownerIdentifier;

	public BinaryIdentifier(BinaryName name, Class<T> type, DomainObjectIdentifier ownerIdentifier) {
		checkArgument(name != null, "Cannot construct a binary identifier because the task name is null.");
		checkArgument(type != null, "Cannot construct a binary identifier because the task type is null.");
		checkArgument(ownerIdentifier != null, "Cannot construct a task identifier because the owner identifier is null.");
		checkArgument(isValidOwner(ownerIdentifier), "Cannot construct a task identifier because the owner identifier is invalid, only ComponentIdentifier and VariantIdentifier are accepted.");
		this.name = name;
		this.type = type;
		this.ownerIdentifier = ownerIdentifier;
	}

	private static boolean isValidOwner(DomainObjectIdentifier ownerIdentifier) {
		return ownerIdentifier instanceof ComponentIdentifier || ownerIdentifier instanceof VariantIdentifier;
	}

	public static <T extends Binary> BinaryIdentifier<T> of(BinaryName name, Class<T> type, DomainObjectIdentifier ownerIdentifier) {
		return new BinaryIdentifier<>(name, type, ownerIdentifier);
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
	public Optional<? extends DomainObjectIdentifier> getParentIdentifier() {
		return Optional.of(ownerIdentifier);
	}

	@Override
	public String getDisplayName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Path getPath() {
		if (getOwnerIdentifier() instanceof DomainObjectIdentifierInternal) {
			return ((DomainObjectIdentifierInternal) getOwnerIdentifier()).getPath().child(name.get());
		}
		return Path.path(name.get());
	}

	@Override
	public String toString() {
		return "binary '" + getPath() + "' (" + type.getSimpleName() + ")";
	}
}
