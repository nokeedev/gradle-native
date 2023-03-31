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

import java.util.Set;

public final class XCProjectLoader implements XCLoader<XCProject, XCProjectReference> {
	private final XCLoader<XCFileReferencesLoader.XCFileReferences, XCProjectReference> fileReferencesLoader;
	private final XCLoader<Set<XCTargetReference>, XCProjectReference> targetReferencesLoader;

	public XCProjectLoader(XCLoader<XCFileReferencesLoader.XCFileReferences, XCProjectReference> fileReferencesLoader, XCLoader<Set<XCTargetReference>, XCProjectReference> targetReferencesLoader) {
		this.fileReferencesLoader = fileReferencesLoader;
		this.targetReferencesLoader = targetReferencesLoader;
	}

	@Override
	public XCProject load(XCProjectReference reference) {
		return new DefaultXCProject(reference, fileReferencesLoader, targetReferencesLoader);
	}
}
