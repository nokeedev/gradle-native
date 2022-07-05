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
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.platform.base.DependencyBucket;
import lombok.EqualsAndHashCode;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkArgument;
import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toGradlePath;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.builder;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketType.from;

@EqualsAndHashCode
public final class DependencyBucketIdentifier implements DomainObjectIdentifier {
	private final DependencyBucketIdentity identity;
	private final DomainObjectIdentifier ownerIdentifier;

	private DependencyBucketIdentifier(DependencyBucketIdentity identity, DomainObjectIdentifier ownerIdentifier) {
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

	// FIXME: Remove this API
	public static DependencyBucketIdentifier of(ElementName name, Class<? extends DependencyBucket> type, DomainObjectIdentifier ownerIdentifier) {
		return new DependencyBucketIdentifier(builder().name(name).type(from(type)).build(), ownerIdentifier);
	}

	public static DependencyBucketIdentifier of(DependencyBucketIdentity identity, DomainObjectIdentifier ownerIdentifier) {
		return new DependencyBucketIdentifier(identity, ownerIdentifier);
	}

	@Override
	public Iterator<Object> iterator() {
		return ImmutableList.builder().addAll(ownerIdentifier).add(identity).build().iterator();
	}

	@Override
	public String toString() {
		return identity.getDisplayName() + " '" + toGradlePath(this) + "'";
	}
}
