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

import dev.nokee.xcode.objects.PBXProjectItem;
import dev.nokee.xcode.objects.Visitable;

import java.util.List;

/**
 * Superclass of build phases. Each build phase represents one step in building a target.
 */
public interface PBXBuildPhase extends PBXProjectItem {
	List<PBXBuildFile> getFiles();

	@Visitable
	<R> R accept(Visitor<R> visitor);

	Builder toBuilder();

	interface Builder {
		PBXBuildPhase build();
	}

	interface Visitor<R> {
		R visit(PBXCopyFilesBuildPhase buildPhase);

		R visit(PBXFrameworksBuildPhase buildPhase);

		R visit(PBXHeadersBuildPhase buildPhase);

		R visit(PBXResourcesBuildPhase buildPhase);

		R visit(PBXShellScriptBuildPhase buildPhase);

		R visit(PBXSourcesBuildPhase buildPhase);
	}
}
