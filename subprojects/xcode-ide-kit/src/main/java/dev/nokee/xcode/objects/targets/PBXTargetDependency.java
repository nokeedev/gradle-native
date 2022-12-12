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
package dev.nokee.xcode.objects.targets;

import dev.nokee.xcode.objects.LenientAwareBuilder;
import dev.nokee.xcode.objects.PBXContainerItemProxy;
import dev.nokee.xcode.objects.PBXProjectItem;
import dev.nokee.xcode.project.CodeablePBXTargetDependency;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;

import java.util.Objects;
import java.util.Optional;

import static dev.nokee.xcode.project.DefaultKeyedObject.key;

/**
 * Element of the {@link PBXTarget#getDependencies()}.
 * Represents a dependency of one target upon another target.
 */
public interface PBXTargetDependency extends PBXProjectItem {
	Optional<String> getName();

	Optional<PBXTarget> getTarget();

	PBXContainerItemProxy getTargetProxy();

	static Builder builder() {
		return new Builder();
	}

	final class Builder implements org.apache.commons.lang3.builder.Builder<PBXTargetDependency>, LenientAwareBuilder<Builder> {
		private String name;
		private PBXTarget target;
		private PBXContainerItemProxy targetProxy;
		private final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();

		public Builder() {
			builder.put(KeyedCoders.ISA, "PBXTargetDependency");
			builder.requires(key(CodeablePBXTargetDependency.CodingKeys.target).or(key(CodeablePBXTargetDependency.CodingKeys.targetProxy)));
		}

		@Override
		public Builder lenient() {
			builder.lenient();
			return this;
		}

		public Builder name(String name) {
			this.name = Objects.requireNonNull(name);
			return this;
		}

		public Builder target(PBXTarget target) {
			this.target = Objects.requireNonNull(target);
			return this;
		}

		public Builder targetProxy(PBXContainerItemProxy targetProxy) {
			this.targetProxy = Objects.requireNonNull(targetProxy);
			return this;
		}

		@Override
		public PBXTargetDependency build() {
			builder.put(CodeablePBXTargetDependency.CodingKeys.name, name);
			builder.put(CodeablePBXTargetDependency.CodingKeys.target, target);
			builder.put(CodeablePBXTargetDependency.CodingKeys.targetProxy, targetProxy);

			return new CodeablePBXTargetDependency(builder.build());
		}
	}
}
