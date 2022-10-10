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
import dev.nokee.buildadapter.xcode.internal.plugins.DefaultXCProjectLocator;
import dev.nokee.buildadapter.xcode.internal.plugins.XCProjectLocator;
import dev.nokee.xcode.XCProjectReference;
import lombok.EqualsAndHashCode;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Supplier;

@EqualsAndHashCode
public final class XCProjectSupplier implements Supplier<Set<XCProjectReference>>, Serializable {
	private final File searchDirectory;
	private final XCProjectLocator locator;

	public XCProjectSupplier(Path searchDirectory) {
		this(searchDirectory, new DefaultXCProjectLocator());
	}

	public XCProjectSupplier(Path searchDirectory, XCProjectLocator locator) {
		this.searchDirectory = searchDirectory.toFile();
		this.locator = locator;
	}


	@Override
	public Set<XCProjectReference> get() {
		return ImmutableSet.copyOf(locator.findProjects(searchDirectory.toPath()));
	}
}
