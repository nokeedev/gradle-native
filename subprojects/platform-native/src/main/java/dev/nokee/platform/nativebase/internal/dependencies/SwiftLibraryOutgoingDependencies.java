/*
 * Copyright 2020 the original author or authors.
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

import lombok.val;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact;
import org.gradle.api.model.ObjectFactory;

public final class SwiftLibraryOutgoingDependencies extends AbstractNativeLibraryOutgoingDependencies implements NativeOutgoingDependencies {
	public SwiftLibraryOutgoingDependencies(Configuration apiElements, Configuration linkElements, Configuration runtimeElements, ObjectFactory objects) {
		super(linkElements, runtimeElements, objects);

		val apiArtifacts = objects.listProperty(PublishArtifact.class);
		apiArtifacts.add(new LazyPublishArtifact(getExportedSwiftModule()));
		apiElements.getOutgoing().getArtifacts().addAllLater(apiArtifacts);
	}
}
