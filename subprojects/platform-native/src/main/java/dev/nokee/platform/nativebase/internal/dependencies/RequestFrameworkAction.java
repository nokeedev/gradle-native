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
package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.runtime.darwin.internal.DarwinLibraryElements;
import dev.nokee.runtime.nativebase.internal.ArtifactSerializationTypes;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.model.ObjectFactory;

public final class RequestFrameworkAction implements Action<ModuleDependency> {
	private static final String NOKEE_MAGIC_FRAMEWORK_GROUP = "dev.nokee.framework";
	private final ObjectFactory objects;

	public RequestFrameworkAction(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void execute(ModuleDependency dependency) {
		if (NOKEE_MAGIC_FRAMEWORK_GROUP.equals(dependency.getGroup())) {
			dependency.attributes(attributes -> {
				attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.class, DarwinLibraryElements.FRAMEWORK_BUNDLE));
				attributes.attribute(ArtifactSerializationTypes.ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, ArtifactSerializationTypes.DESERIALIZED);
			});
		}
	}
}
