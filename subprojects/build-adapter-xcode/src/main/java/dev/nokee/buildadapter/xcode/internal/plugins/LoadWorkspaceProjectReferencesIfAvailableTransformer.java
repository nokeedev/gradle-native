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

import com.google.common.collect.ImmutableSet;
import dev.nokee.xcode.XCLoader;
import dev.nokee.xcode.XCLoaders;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCWorkspaceReference;
import lombok.EqualsAndHashCode;
import org.gradle.api.Transformer;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Set;
import java.util.function.Supplier;

@EqualsAndHashCode
public final class LoadWorkspaceProjectReferencesIfAvailableTransformer implements Transformer<Set<XCProjectReference>, XCWorkspaceReference>, Serializable {
	private final Supplier<Set<XCProjectReference>> projectSupplier;
	private final XCLoader<Iterable<XCProjectReference>, XCWorkspaceReference> projectReferencesLoader;

	public LoadWorkspaceProjectReferencesIfAvailableTransformer(Supplier<Set<XCProjectReference>> defaultProjectsSupplier) {
		this(defaultProjectsSupplier, XCLoaders.workspaceProjectReferencesLoader());
	}

	public LoadWorkspaceProjectReferencesIfAvailableTransformer(Supplier<Set<XCProjectReference>> projectSupplier, XCLoader<Iterable<XCProjectReference>, XCWorkspaceReference> projectReferencesLoader) {
		this.projectSupplier = projectSupplier;
		this.projectReferencesLoader = projectReferencesLoader;
	}

	@Override
	public Set<XCProjectReference> transform(@Nullable XCWorkspaceReference workspaceReference) {
		if (workspaceReference == null) {
			return projectSupplier.get();
		} else {
			return ImmutableSet.copyOf(workspaceReference.load(projectReferencesLoader));
		}
	}
}
