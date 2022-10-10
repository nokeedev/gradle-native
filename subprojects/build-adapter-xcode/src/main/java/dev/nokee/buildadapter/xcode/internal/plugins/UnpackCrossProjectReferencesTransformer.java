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
package dev.nokee.buildadapter.xcode.internal.plugins;

import dev.nokee.xcode.XCLoader;
import dev.nokee.xcode.XCLoaders;
import dev.nokee.xcode.XCProjectReference;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.Transformer;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Recursively expand every located projects to include their cross-project references.
 */
@EqualsAndHashCode
public final class UnpackCrossProjectReferencesTransformer implements Transformer<Set<XCProjectReference>, Set<XCProjectReference>>, Serializable {
	private final XCLoader<Iterable<XCProjectReference>, XCProjectReference> projectReferencesLoader;

	public UnpackCrossProjectReferencesTransformer() {
		this(XCLoaders.crossProjectReferencesLoader());
	}

	public UnpackCrossProjectReferencesTransformer(XCLoader<Iterable<XCProjectReference>, XCProjectReference> projectReferencesLoader) {
		this.projectReferencesLoader = projectReferencesLoader;
	}

	@Override
	public Set<XCProjectReference> transform(Set<XCProjectReference> projectReferences) {
		val result = new LinkedHashSet<XCProjectReference>();
		val queue = new ArrayDeque<>(projectReferences);
		while (!queue.isEmpty()) {
			val reference = queue.pop();
			if (result.add(reference)) {
				reference.load(projectReferencesLoader).forEach(queue::add);
			}
		}
		return result;
	}
}
