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
package dev.nokee.xcode.objects.buildphase;

import com.google.common.collect.ImmutableList;
import dev.nokee.xcode.objects.PBXProjectItem;

import java.util.List;

/**
 * Superclass of build phases. Each build phase represents one step in building a target.
 */
public abstract class PBXBuildPhase extends PBXProjectItem {
	private final ImmutableList<PBXBuildFile> files;

	protected PBXBuildPhase(ImmutableList<PBXBuildFile> files) {
		this.files = files;
	}

	public List<PBXBuildFile> getFiles() {
		return files;
	}
}
