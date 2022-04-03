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

import com.google.common.collect.Iterables;
import dev.nokee.xcode.XCWorkspaceReference;
import org.gradle.api.Transformer;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public final class SelectXCWorkspaceLocationTransformation implements Transformer<XCWorkspaceReference, Iterable<XCWorkspaceReference>> {
	private static final Logger LOGGER = Logging.getLogger(SelectXCWorkspaceLocationTransformation.class);

	@Override
	public XCWorkspaceReference transform(Iterable<XCWorkspaceReference> workspaceLocations) {
		XCWorkspaceReference result = null;
		switch (Iterables.size(workspaceLocations)) {
			case 0: break;
			case 1:
				result = workspaceLocations.iterator().next();
				break;
			default:
				result = workspaceLocations.iterator().next();
				LOGGER.warn(String.format("The plugin 'dev.nokee.xcode-build-adapter' will use Xcode workspace located at '%s' because multiple Xcode workspace were found in '%s'. See https://nokee.fyi/using-xcode-build-adapter for more details.", result, result.getLocation().getParent()));
				break;
		}
		return result;
	}
}
