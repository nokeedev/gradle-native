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
package dev.nokee.xcode.objects.files;

import dev.nokee.xcode.objects.LenientAwareBuilder;
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.project.CodeablePBXVariantGroup;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;

import javax.annotation.Nullable;
import java.util.List;

import static dev.nokee.xcode.project.DefaultKeyedObject.key;

/**
 * Represents localized resources.
 */
public interface PBXVariantGroup extends PBXGroupElement, GroupChild, PBXBuildFile.FileReference {
	static Builder builder() {
		return new Builder();
	}

	final class Builder extends PBXGroupElement.Builder<Builder, PBXVariantGroup> implements LenientAwareBuilder<Builder> {
		private final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();

		public Builder() {
			builder.put(KeyedCoders.ISA, "PBXVariantGroup");
			builder.requires(key(CodeablePBXVariantGroup.CodingKeys.sourceTree));
		}

		@Override
		public Builder lenient() {
			builder.lenient();
			return this;
		}

		@Override
		protected PBXVariantGroup newGroupElement(@Nullable String name, @Nullable String path, @Nullable PBXSourceTree sourceTree, List<GroupChild> children) {
			// mainGroup can have both null name and path
			builder.putNullable(CodeablePBXVariantGroup.CodingKeys.name, name);
			builder.putNullable(CodeablePBXVariantGroup.CodingKeys.path, path);
			builder.putNullable(CodeablePBXVariantGroup.CodingKeys.sourceTree, sourceTree);
			builder.put(CodeablePBXVariantGroup.CodingKeys.children, children);

			return new CodeablePBXVariantGroup(builder.build());
		}
	}
}
