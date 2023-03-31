/*
 * Copyright 2023 the original author or authors.
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
import lombok.EqualsAndHashCode;

import java.util.Set;

@EqualsAndHashCode
public final class DefaultXCProject implements XCProject {
	private final XCProjectReference reference;
	private transient XCLoader<XCFileReferencesLoader.XCFileReferences, XCProjectReference> fileReferencesLoader;
	private transient XCLoader<Set<XCTargetReference>, XCProjectReference> targetReferencesLoader;
	private transient XCFileReferencesLoader.XCFileReferences references;

	// friends with XCProjectReference
	DefaultXCProject(XCProjectReference reference, XCLoader<XCFileReferencesLoader.XCFileReferences, XCProjectReference> fileReferencesLoader, XCLoader<Set<XCTargetReference>, XCProjectReference> targetReferencesLoader) {
		this.reference = reference;
		this.fileReferencesLoader = fileReferencesLoader;
		this.targetReferencesLoader = targetReferencesLoader;
	}

	public String getName() {
		return reference.getName();
	}

	public Set<XCTarget> getTargets() {
		return reference.load(targetReferencesLoader).stream().map(XCTargetReference::load).collect(ImmutableSet.toImmutableSet());
	}

	public XCProjectReference toReference() {
		return reference;
	}

	private XCFileReferencesLoader.XCFileReferences getFileReferences() {
		if (references == null) {
			references = fileReferencesLoader.load(toReference());
			fileReferencesLoader = null;
		}
		return references;
	}
}
