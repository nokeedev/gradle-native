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
import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.VariantIdentifier;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.util.Path;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.builder;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketType.from;

@EqualsAndHashCode
public class DependencyBucketIdentifier implements DomainObjectIdentifierInternal {
	private final DependencyBucketIdentity identity;
	private final Class<?> type;
	private final DomainObjectIdentifier ownerIdentifier;

	private DependencyBucketIdentifier(DependencyBucketIdentity identity, Class<?> type, DomainObjectIdentifier ownerIdentifier) {
		checkArgument(type != null, "Cannot construct a dependency identifier because the bucket type is null.");
		checkArgument(ownerIdentifier != null, "Cannot construct a dependency identifier because the owner identifier is null.");
		checkArgument(isValidOwner(ownerIdentifier), "Cannot construct a dependency identifier because the owner identifier is invalid, only ProjectIdentifier, ComponentIdentifier and VariantIdentifier are accepted.");
		this.identity = identity;
		this.type = type;
		this.ownerIdentifier = ownerIdentifier;
	}

	private static boolean isValidOwner(DomainObjectIdentifier ownerIdentifier) {
		return ownerIdentifier instanceof ProjectIdentifier || ownerIdentifier instanceof ComponentIdentifier || ownerIdentifier instanceof VariantIdentifier;
	}

	public DependencyBucketName getName() {
		return identity.getName();
	}

	@Deprecated
	public Class<?> getType() {
		return type;
	}

	public DomainObjectIdentifier getOwnerIdentifier() {
		return ownerIdentifier;
	}

	@Override
	public Optional<? extends DomainObjectIdentifier> getParentIdentifier() {
		return Optional.of(ownerIdentifier);
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

	@Deprecated
	@Override
	public String getDisplayName() {
		return DependencyBuckets.toDescription(this);
	}

	@Override
	public Path getPath() {
		if (ownerIdentifier instanceof DomainObjectIdentifierInternal) {
			return ((DomainObjectIdentifierInternal) getOwnerIdentifier()).getPath().child(getName().get());
		}
		return Path.path(getName().get());
	}

	@Deprecated
	public static DependencyBucketIdentifier of(DependencyBucketName name, Class<? extends DependencyBucket> type, DomainObjectIdentifier ownerIdentifier) {
		return new DependencyBucketIdentifier(builder().name(name).type(from(type)).build(), type, ownerIdentifier);
	}

	public static DependencyBucketIdentifier of(DependencyBucketIdentity identity, DomainObjectIdentifier ownerIdentifier) {
		return new DependencyBucketIdentifier(identity, identity.getType().toBucketType(), ownerIdentifier);
	}

	@Override
	public Iterator<Object> iterator() {
		val builder = ImmutableList.builder();
		getComponentOwnerIdentifier().ifPresent(identifier -> {
			builder.add(identifier.getProjectIdentifier());
			builder.add(identifier);
		});
		getVariantOwnerIdentifier().ifPresent(builder::add);
		if (!getComponentOwnerIdentifier().isPresent() && !getVariantOwnerIdentifier().isPresent()) {
			builder.add(getOwnerIdentifier());
		}
		builder.add(identity);
		return builder.build().iterator();
	}
}
