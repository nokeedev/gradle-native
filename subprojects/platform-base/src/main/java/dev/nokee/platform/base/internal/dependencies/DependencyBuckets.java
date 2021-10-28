/*
 * Copyright 2021 the original author or authors.
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

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentIdentity;
import dev.nokee.platform.base.internal.HasName;
import dev.nokee.platform.base.internal.VariantIdentifier;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DependencyBuckets {
	public static String toDescription(DependencyBucketIdentifier identifier) {
		val builder = new StringBuilder();
		val identities = (Iterable<Object>) identifier;
		val identity = Iterables.getLast(identifier);
		builder.append(StringUtils.capitalize(identity.toString()));
		builder.append(" for ");
		if (Iterables.size(identities) == 1) {
			builder.append("<unknown>");
		} else {
			val ownerIdentifier = Iterables.get(identities, Iterables.size(identities) - 2);
			if (ownerIdentifier instanceof DomainObjectIdentifierInternal) {
				builder.append(((DomainObjectIdentifierInternal) ownerIdentifier).getDisplayName());
			} else if (ownerIdentifier instanceof ComponentIdentifier) {
				builder.append(ownerIdentifier);
			} else {
				builder.append("<unknown>");
			}
		}
		builder.append(".");
		return builder.toString();
	}

	public static ModelPath toPath(DependencyBucketIdentifier identifier) {
		return ModelPath.path(Streams.stream(identifier).flatMap(it -> {
			if (it instanceof ProjectIdentifier) {
				return Stream.empty();
			} else if (it instanceof HasName) {
				return Stream.of(((HasName) it).getName().toString());
			} else if (it instanceof VariantIdentifier) {
				return Stream.of(((VariantIdentifier<?>) it).getUnambiguousName());
			} else {
				throw new UnsupportedOperationException();
			}
		}).collect(Collectors.toList()));
	}

	public static String configurationName(DependencyBucketIdentifier identifier) {
		return StringUtils.uncapitalize(Streams.stream(identifier)
			.flatMap(it -> {
				if (it instanceof ProjectIdentifier) {
					return Stream.empty();
				} else if (it instanceof ComponentIdentifier) {
					if (((ComponentIdentifier) it).isMainComponent()) {
						return Stream.empty();
					} else {
						return Stream.of(((ComponentIdentifier) it).getName().get());
					}
				} else if (it instanceof ComponentIdentity) {
					if (((ComponentIdentity) it).isMainComponent()) {
						return Stream.empty();
					} else {
						return Stream.of(((ComponentIdentity) it).getName().get());
					}
				} else if (it instanceof VariantIdentifier) {
					return Stream.of(((VariantIdentifier<?>) it).getUnambiguousName());
				} else if (it instanceof HasName) {
					return Stream.of(((HasName) it).getName().toString());
				} else {
					throw new UnsupportedOperationException();
				}
			})
			.map(StringUtils::capitalize)
			.collect(Collectors.joining()));
	}
}
