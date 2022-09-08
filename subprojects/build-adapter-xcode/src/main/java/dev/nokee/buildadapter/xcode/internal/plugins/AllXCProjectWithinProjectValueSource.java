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

import dev.nokee.xcode.XCProjectReference;
import lombok.val;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;

import java.util.ArrayDeque;
import java.util.LinkedHashSet;

@SuppressWarnings("UnstableApiUsage")
public abstract class AllXCProjectWithinProjectValueSource implements ValueSource<Iterable<XCProjectReference>, AllXCProjectWithinProjectValueSource.Parameters> {
	interface Parameters extends ValueSourceParameters {
		ListProperty<XCProjectReference> getProjectLocations();
	}

	@Override
	public Iterable<XCProjectReference> obtain() {
		val result = new LinkedHashSet<XCProjectReference>();
		val queue = new ArrayDeque<>(getParameters().getProjectLocations().get());
		while (!queue.isEmpty()) {
			val reference = queue.pop();
			if (result.add(reference)) {
				// TODO: They may be an issue after the first config-cache reuse where things can change but not considered because the the load use XCCache. The XCLoaderService close may not be called on the first config-cache reuse which could leave a cache entry that becomes wrong so the second config-cache check will be wrong by returning an invalid cached project. We should functionally test using a config-cache reusing project and then change the pbxproj to include an additional cross-reference (or remove a cross-reference), we should expect the config-cache to not be reused.
				//  I wonder if we could force the service to close after a config-cache attempt if we use the service as part of the ValueSourceParameters. It would mark the service as used and should trigger the close.
				//  Actually, in fact, given the service is "used", it should close at the end of the config-cache reused because it's still a build... but what happen when a build service is only used during config phase?
				queue.addAll(reference.load().getProjectReferences());
			}
		}
		return result;
	}
}
