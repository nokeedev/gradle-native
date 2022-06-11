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
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;

import javax.annotation.Nullable;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public abstract class AllXCProjectLocationsValueSource implements ValueSource<Iterable<XCProjectReference>, AllXCProjectLocationsValueSource.Parameters> {
	private static final XCProjectLocator XCODE_PROJECT_LOCATOR = new XCProjectLocator();

	interface Parameters extends ValueSourceParameters {
		DirectoryProperty getSearchDirectory();
	}

	@Nullable
	@Override
	public Iterable<XCProjectReference> obtain() {
		return XCODE_PROJECT_LOCATOR.findProjects(getParameters().getSearchDirectory().get().getAsFile().toPath()).stream().map(XCProjectReference::of).collect(Collectors.toList());
	}
}
