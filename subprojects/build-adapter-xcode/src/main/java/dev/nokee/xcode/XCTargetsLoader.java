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

import com.google.common.collect.ImmutableSet;
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.targets.PBXTarget;

import java.io.Serializable;
import java.util.Set;

public final class XCTargetsLoader implements XCLoader<Set<XCTargetReference>, XCProjectReference>, Serializable {
	private final XCLoader<PBXProject, XCProjectReference> pbxprojectLoader;

	public XCTargetsLoader(XCLoader<PBXProject, XCProjectReference> pbxprojectLoader) {
		this.pbxprojectLoader = pbxprojectLoader;
	}

	@Override
	public Set<XCTargetReference> load(XCProjectReference reference) {
		return pbxprojectLoader.load(reference).getTargets().stream().map(PBXTarget::getName).map(reference::ofTarget).collect(ImmutableSet.toImmutableSet());
	}
}
