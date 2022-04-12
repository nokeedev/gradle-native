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

import com.google.common.base.Preconditions;
import dev.nokee.xcode.objects.PBXContainerItem;

import javax.annotation.Nullable;

/**
 * Superclass for file, directories, and groups. Xcode's virtual file hierarchy are made of these
 * objects.
 */
public abstract class PBXReference extends PBXContainerItem {
	private final String name;
	@Nullable private final String path;
	/**
	 * The "base" path of the reference. The absolute path is resolved by prepending the resolved
	 * base path.
	 */
	private final PBXSourceTree sourceTree;

	public PBXReference(String name, @Nullable String path, PBXSourceTree sourceTree) {
		this.name = Preconditions.checkNotNull(name);
		this.path = path;
		this.sourceTree = Preconditions.checkNotNull(sourceTree);
	}

	public String getName() {
		return name;
	}

	@Nullable
	public String getPath() {
		return path;
	}

	public PBXSourceTree getSourceTree() {
		return sourceTree;
	}

	@Override
	public int stableHash() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return String.format(
			"%s name=%s path=%s sourceTree=%s",
			super.toString(),
			getName(),
			getPath(),
			getSourceTree());
	}

}
