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

import com.google.common.base.Preconditions;
import dev.nokee.model.HasName;
import dev.nokee.model.internal.names.ElementName;
import lombok.EqualsAndHashCode;
import lombok.val;

import java.util.Objects;

import static dev.nokee.platform.base.internal.dependencies.DependencyBuckets.defaultDisplayName;
import static dev.nokee.platform.base.internal.dependencies.DependencyBuckets.defaultDisplayNameOfDeclarableBucket;

@EqualsAndHashCode
public final class DependencyBucketIdentity implements HasName {
	public static DependencyBucketIdentity declarable(String name) {
		val bucketName = ElementName.of(name);
		return new DependencyBucketIdentity(bucketName, DependencyBucketType.Declarable, defaultDisplayNameOfDeclarableBucket(bucketName));
	}

	public static DependencyBucketIdentity consumable(String name) {
		val bucketName = ElementName.of(name);
		return new DependencyBucketIdentity(bucketName, DependencyBucketType.Consumable, defaultDisplayName(bucketName));
	}

	public static DependencyBucketIdentity resolvable(String name) {
		val bucketName = ElementName.of(name);
		return new DependencyBucketIdentity(bucketName, DependencyBucketType.Resolvable, defaultDisplayName(bucketName));
	}

	private final ElementName name;
	private final DependencyBucketType type;
	private final String displayName;

	private DependencyBucketIdentity(ElementName name, DependencyBucketType type, String displayName) {
		this.name = Objects.requireNonNull(name);
		this.type = Objects.requireNonNull(type);
		this.displayName = Objects.requireNonNull(displayName);
	}

	@Override
	public ElementName getName() {
		return name;
	}

	public DependencyBucketType getType() {
		return type;
	}

	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String displayName;
		private ElementName name;
		private DependencyBucketType type = DependencyBucketType.Declarable;

		public Builder name(ElementName name) {
			this.name = Objects.requireNonNull(name);
			return this;
		}

		public Builder type(DependencyBucketType type) {
			this.type = Objects.requireNonNull(type);
			return this;
		}

		public Builder displayName(String displayName) {
			this.displayName = Objects.requireNonNull(displayName);
			return this;
		}

		public DependencyBucketIdentity build() {
			Preconditions.checkState(name != null);

			if (displayName == null) {
				if (type == DependencyBucketType.Declarable) {
					displayName = defaultDisplayNameOfDeclarableBucket(name);
				} else {
					displayName = defaultDisplayName(name);
				}
			}

			return new DependencyBucketIdentity(name, type, displayName);
		}
	}
}
