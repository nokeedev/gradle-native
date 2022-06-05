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

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * This is the element for referencing localized resources.
 */
public final class PBXVariantGroup extends PBXGroupElement {
	public PBXVariantGroup(@Nullable String name, @Nullable String path, PBXSourceTree sourceTree, List<PBXReference> children) {
		super(name, path, sourceTree, children);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder extends PBXGroupElement.Builder<Builder, PBXVariantGroup> {
		@Override
		protected PBXVariantGroup newGroupElement(@Nullable String name, @Nullable String path, @Nullable PBXSourceTree sourceTree, List<PBXReference> children) {
			// mainGroup can have both null name and path
			return new PBXVariantGroup(name, path, Objects.requireNonNull(sourceTree, "'sourceTree' must not be null"), ImmutableList.copyOf(children));
		}
	}
}
