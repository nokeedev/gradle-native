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
import dev.nokee.xcode.XCProjectReference;
import org.gradle.api.Transformer;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public final class SelectXCProjectLocationTransformation implements Transformer<XCProjectReference, Iterable<XCProjectReference>> {
	private static final Logger LOGGER = Logging.getLogger(SelectXCProjectLocationTransformation.class);

	@Override
	public XCProjectReference transform(Iterable<XCProjectReference> projectLocations) {
		XCProjectReference result = null;
		switch (Iterables.size(projectLocations)) {
			case 0: break;
			case 1:
				result = projectLocations.iterator().next();
				break;
			default:
				result = projectLocations.iterator().next();
				LOGGER.warn(String.format("The plugin 'dev.nokee.xcode-build-adapter' will use Xcode project located at '%s' because multiple Xcode project were found without workspace in '%s'. See https://nokee.fyi/using-xcode-build-adapter for more details.", result.getLocation(), result.getLocation().getParent()));
				break;
		}
		return result;
	}
}
