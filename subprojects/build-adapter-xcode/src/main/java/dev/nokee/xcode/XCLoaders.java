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

import dev.nokee.buildadapter.xcode.internal.plugins.specs.XCBuildSpec;
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.targets.PBXTarget;
import dev.nokee.xcode.workspace.XCWorkspaceData;

import java.util.Set;

public final class XCLoaders {
	private static final XCLoader<PBXProject, XCProjectReference> PBXPROJECT_LOADER = new XCCacheLoader<>(new PBXProjectLoader());
	private static final XCLoader<PBXTarget, XCTargetReference> PBXTARGET_LOADER = new XCCacheLoader<>(new PBXTargetLoader(PBXPROJECT_LOADER));
	private static final XCLoader<XCFileReferencesLoader.XCFileReferences, XCProjectReference> FILE_REFERENCES_LOADER = new XCCacheLoader<>(new XCFileReferencesLoader(PBXPROJECT_LOADER));
	private static final XCLoader<XCWorkspaceData, XCWorkspaceReference> WORKSPACE_DATA_LOADER = new XCCacheLoader<>(new XCWorkspaceDataLoader());
	private static final XCLoader<Iterable<XCProjectReference>, XCWorkspaceReference> WORKSPACE_PROJECT_REFERENCES_LOADER = new XCCacheLoader<>(new WorkspaceProjectReferencesLoader(WORKSPACE_DATA_LOADER, new DefaultXCProjectReferenceFactory()));
	private static final XCLoader<Iterable<XCProjectReference>, XCProjectReference> CROSS_PROJECT_REFERENCES_LOADER = new XCCacheLoader<>(new CrossProjectReferencesLoader(PBXPROJECT_LOADER, FILE_REFERENCES_LOADER));
	private static final XCLoader<XCProject, XCProjectReference> PROJECT_LOADER = new XCCacheLoader<>(new XCProjectLoader(PBXPROJECT_LOADER, FILE_REFERENCES_LOADER));
	private static final XCLoader<XCTarget, XCTargetReference> TARGET_LOADER = new XCCacheLoader<>(new XCTargetLoader(PBXPROJECT_LOADER, FILE_REFERENCES_LOADER));
	private static final XCLoader<Set<XCTargetReference>, XCProjectReference> ALL_TARGETS_LOADER = new XCCacheLoader<>(new XCTargetsLoader(PBXPROJECT_LOADER));
	private static final XCLoader<Set<String>, XCTargetReference> TARGET_CONFIGURATION_LOADER = new XCCacheLoader<>(new ConfigurationsLoader(PBXPROJECT_LOADER));

	private static final XCLoader<String, XCTargetReference> DEFAULT_TARGET_CONFIGURATION_LOADER = new XCCacheLoader<>(new DefaultTargetConfigurationLoader(PBXPROJECT_LOADER));

	private static final XCLoader<XCBuildSpec, XCTargetReference> BUILD_SPEC_LOADER = new XCCacheLoader<>(new XCBuildSpecLoader(PBXTARGET_LOADER));

	public static XCLoader<Iterable<XCProjectReference>, XCProjectReference> crossProjectReferencesLoader() {
		return CROSS_PROJECT_REFERENCES_LOADER;
	}

	public static XCLoader<Iterable<XCProjectReference>, XCWorkspaceReference> workspaceProjectReferencesLoader() {
		return WORKSPACE_PROJECT_REFERENCES_LOADER;
	}

	public static XCLoader<XCProject, XCProjectReference> projectLoader() {
		return PROJECT_LOADER;
	}

	public static XCLoader<PBXTarget, XCTargetReference> pbxtargetLoader() {
		return PBXTARGET_LOADER;
	}

	public static XCLoader<XCTarget, XCTargetReference> targetLoader() {
		return TARGET_LOADER;
	}

	public static XCLoader<Set<XCTargetReference>, XCProjectReference> allTargetsLoader() {
		return ALL_TARGETS_LOADER;
	}

	public static XCLoader<Set<String>, XCTargetReference> targetConfigurationsLoader() {
		return TARGET_CONFIGURATION_LOADER;
	}

	public static XCLoader<String, XCTargetReference> defaultTargetConfigurationLoader() {
		return DEFAULT_TARGET_CONFIGURATION_LOADER;
	}

	public static XCLoader<XCBuildSpec, XCTargetReference> buildSpecLoader() {
		return BUILD_SPEC_LOADER;
	}
}
