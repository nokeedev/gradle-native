/*
 * Copyright 2022 the original author or authors.
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

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.core.DisplayName;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Objects;

public final class DependencyBucketDescription {
	private final DisplayName displayName;
	@Nullable private final DomainObjectIdentifier forOwner;

	public DependencyBucketDescription(DisplayName displayName) {
		this(displayName, null);
	}

	private DependencyBucketDescription(DisplayName displayName, @Nullable DomainObjectIdentifier forOwner) {
		this.displayName = displayName;
		this.forOwner = forOwner;
	}

	public static DependencyBucketDescription of(DisplayName displayName) {
		return new DependencyBucketDescription(Objects.requireNonNull(displayName));
	}

	public DependencyBucketDescription forOwner(DomainObjectIdentifier identifier) {
		return new DependencyBucketDescription(displayName, Objects.requireNonNull(identifier));
	}

	public String toString() {
		val builder = new StringBuilder();
		builder.append(StringUtils.capitalize(displayName.toString()));
		if (forOwner != null) {
			builder.append(" for ");
			builder.append(forOwner);
		}
		builder.append(".");
		return builder.toString();
	}
}
