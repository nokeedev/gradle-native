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
package dev.nokee.xcode;

import dev.nokee.xcode.objects.PBXProject;

public final class XCLoaders {
	private static final XCLoader<PBXProject, XCProjectReference> PBXPROJECT_LOADER = new XCCacheLoader<>(new PBXProjectLoader());
	private static final XCLoader<XCFileReferencesLoader.XCFileReferences, XCProjectReference> FILE_REFERENCES_LOADER = new XCCacheLoader<>(new XCFileReferencesLoader(PBXPROJECT_LOADER));
	private static final XCLoader<Iterable<XCProjectReference>, XCWorkspaceReference> WORKSPACE_PROJECT_REFERENCES_LOADER = new XCCacheLoader<>(new WorkspaceProjectReferencesLoader());
	private static final XCLoader<Iterable<XCProjectReference>, XCProjectReference> CROSS_PROJECT_REFERENCES_LOADER = new XCCacheLoader<>(new CrossProjectReferencesLoader(PBXPROJECT_LOADER, FILE_REFERENCES_LOADER));
	private static final XCLoader<XCProject, XCProjectReference> PROJECT_LOADER = new XCCacheLoader<>(new XCProjectLoader(PBXPROJECT_LOADER, FILE_REFERENCES_LOADER));
	private static final XCLoader<XCTarget, XCTargetReference> TARGET_LOADER = new XCCacheLoader<>(new XCTargetLoader(PBXPROJECT_LOADER, FILE_REFERENCES_LOADER));

	public static XCLoader<Iterable<XCProjectReference>, XCProjectReference> crossProjectReferencesLoader() {
		return CROSS_PROJECT_REFERENCES_LOADER;
	}

	public static XCLoader<Iterable<XCProjectReference>, XCWorkspaceReference> workspaceProjectReferencesLoader() {
		return WORKSPACE_PROJECT_REFERENCES_LOADER;
	}

	public static XCLoader<XCProject, XCProjectReference> projectLoader() {
		return PROJECT_LOADER;
	}

	public static XCLoader<XCTarget, XCTargetReference> targetLoader() {
		return TARGET_LOADER;
	}
}
