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

import com.google.common.collect.ImmutableList;
import dev.nokee.xcode.objects.LenientAwareBuilder;
import dev.nokee.xcode.project.CodeablePBXGroup;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;

import javax.annotation.Nullable;
import java.util.List;

import static dev.nokee.xcode.project.DefaultKeyedObject.key;

/**
 * A collection of files in Xcode's virtual filesystem hierarchy.
 */
public interface PBXGroup extends PBXGroupElement, GroupChild {
	// It seems the name or path can be null but not both which is a bit different from PBXFileReference.
	//   Except for the mainGroup which both the name and path is null

	static Builder builder() {
		return new Builder();
	}

	final class Builder extends PBXGroupElement.Builder<Builder, PBXGroup> implements LenientAwareBuilder<Builder> {
		private final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();

		public Builder() {
			builder.put(KeyedCoders.ISA, "PBXGroup");
			builder.requires(key(CodeablePBXGroup.CodingKeys.sourceTree));
		}

		@Override
		public Builder lenient() {
			builder.lenient();
			return this;
		}

		@Override
		protected PBXGroup newGroupElement(@Nullable String name, @Nullable String path, @Nullable PBXSourceTree sourceTree, List<GroupChild> children) {
			// mainGroup can have both null name and path
			builder.put(CodeablePBXGroup.CodingKeys.name, name);
			builder.put(CodeablePBXGroup.CodingKeys.path, path);
			builder.put(CodeablePBXGroup.CodingKeys.sourceTree, sourceTree);
			builder.put(CodeablePBXGroup.CodingKeys.children, ImmutableList.copyOf(children));

			return new CodeablePBXGroup(builder.build());
		}
	}
}
