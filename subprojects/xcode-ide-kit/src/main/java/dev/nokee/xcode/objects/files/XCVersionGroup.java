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

import javax.annotation.Nullable;
import java.util.List;

public final class XCVersionGroup extends PBXGroupElement implements GroupChild {
	private XCVersionGroup(@Nullable String name, @Nullable String path, PBXSourceTree sourceTree, List<GroupChild> children) {
		super(name, path, sourceTree, children);
	}

	@Override
	public String toString() {
		return String.format(
			"%s isa=%s name=%s path=%s sourceTree=%s children=%s",
			super.toString(),
			this.getClass().getSimpleName(),
			getName().orElse(null),
			getPath().orElse(null),
			getSourceTree(),
			getChildren());
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder extends PBXGroupElement.Builder<Builder, XCVersionGroup> {
		@Override
		protected XCVersionGroup newGroupElement(@Nullable String name, @Nullable String path, @Nullable PBXSourceTree sourceTree, List<GroupChild> children) {
			return new XCVersionGroup(name, path, sourceTree, children);
		}
	}
}
