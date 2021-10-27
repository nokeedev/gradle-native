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
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.platform.base.internal.VariantIdentifier;
import lombok.Value;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.util.GUtil;
import org.gradle.util.Path;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

@Value
public class DependencyBucketIdentifier<T extends DependencyBucket> implements DomainObjectIdentifierInternal {
	DependencyBucketName name;
	Class<?> type;
	DomainObjectIdentifier ownerIdentifier;

	private DependencyBucketIdentifier(DependencyBucketName name, Class<?> type, DomainObjectIdentifier ownerIdentifier) {
		checkArgument(name != null, "Cannot construct a dependency identifier because the bucket name is null.");
		checkArgument(type != null, "Cannot construct a dependency identifier because the bucket type is null.");
		checkArgument(ownerIdentifier != null, "Cannot construct a dependency identifier because the owner identifier is null.");
		checkArgument(isValidOwner(ownerIdentifier), "Cannot construct a dependency identifier because the owner identifier is invalid, only ProjectIdentifier, ComponentIdentifier and VariantIdentifier are accepted.");
		this.name = name;
		this.type = type;
		this.ownerIdentifier = ownerIdentifier;
	}

	private static boolean isValidOwner(DomainObjectIdentifier ownerIdentifier) {
		return ownerIdentifier instanceof ProjectIdentifier || ownerIdentifier instanceof ComponentIdentifier || ownerIdentifier instanceof VariantIdentifier;
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

	@Override
	public String getDisplayName() {
		val builder = new StringBuilder();
		builder.append(StringUtils.capitalize(GUtil.toWords(name.get()).replace("api", "API")));
		if (!ConsumableDependencyBucket.class.isAssignableFrom(type) && !ResolvableDependencyBucket.class.isAssignableFrom(type)) {
			builder.append(" dependencies");
		}
		builder.append(" for ");
		if (ownerIdentifier instanceof DomainObjectIdentifierInternal) {
			builder.append(((DomainObjectIdentifierInternal) ownerIdentifier).getDisplayName());
		} else if (ownerIdentifier instanceof ComponentIdentifier) {
			builder.append(ownerIdentifier);
		} else {
			builder.append("<unknown>");
		}
		builder.append(".");
		return builder.toString();
	}

	@Override
	public Path getPath() {
		if (ownerIdentifier instanceof DomainObjectIdentifierInternal) {
			return ((DomainObjectIdentifierInternal) getOwnerIdentifier()).getPath().child(name.get());
		}
		return Path.path(name.get());
	}

	public String getConfigurationName() {
		val segments = new ArrayList<String>();

		getComponentOwnerIdentifier()
			.filter(it -> !it.isMainComponent())
			.map(ComponentIdentifier::getName)
			.map(ComponentName::get)
			.ifPresent(segments::add);
		getVariantOwnerIdentifier()
			.map(VariantIdentifier::getUnambiguousName)
			.filter(it -> !it.isEmpty())
			.ifPresent(segments::add);
		segments.add(name.get());

		return StringUtils.uncapitalize(segments.stream().map(StringUtils::capitalize).collect(Collectors.joining()));
	}

	public static DependencyBucketIdentifier<?> of(DependencyBucketName name, Class<? extends DependencyBucket> type, DomainObjectIdentifier ownerIdentifier) {
		return new DependencyBucketIdentifier<>(name, type, ownerIdentifier);
	}

	@Override
	public Iterator<Object> iterator() {
		val builder = ImmutableList.builder();
		getComponentOwnerIdentifier().ifPresent(identifier -> {
			builder.add(identifier.getProjectIdentifier());
			builder.add(identifier);
		});
		getVariantOwnerIdentifier().ifPresent(builder::add);
		builder.add(this);
		return builder.build().iterator();
	}
}
