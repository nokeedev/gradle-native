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
import lombok.val;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;

import javax.inject.Inject;
import java.util.ArrayDeque;
import java.util.LinkedHashSet;

@SuppressWarnings("UnstableApiUsage")
public abstract class AllXCProjectReferencesValueSource implements ValueSource<Iterable<XCProjectReference>, AllXCProjectReferencesValueSource.Parameters> {
	private final XCLoader<Iterable<XCProjectReference>, XCProjectReference> projectReferencesLoader;

	public interface Parameters extends ValueSourceParameters {
		ListProperty<XCProjectReference> getProjectLocations();
	}

	@Inject
	public AllXCProjectReferencesValueSource() {
		this(XCLoaders.crossProjectReferencesLoader());
	}

	public AllXCProjectReferencesValueSource(XCLoader<Iterable<XCProjectReference>, XCProjectReference> projectReferencesLoader) {
		this.projectReferencesLoader = projectReferencesLoader;
	}

	@Override
	public Iterable<XCProjectReference> obtain() {
		val result = new LinkedHashSet<XCProjectReference>();
		val queue = new ArrayDeque<>(getParameters().getProjectLocations().get());
		while (!queue.isEmpty()) {
			val reference = queue.pop();
			if (result.add(reference)) {
				reference.load(projectReferencesLoader).forEach(queue::add);
			}
		}
		return result;
	}
}
