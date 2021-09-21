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
package dev.nokee.ide.xcode.internal;

import dev.nokee.ide.xcode.XcodeIdeBuildConfiguration;
import dev.nokee.utils.ConfigureUtils;
import lombok.Getter;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import static dev.nokee.utils.ConfigureUtils.configureDisplayName;

public final class DefaultXcodeIdeBuildConfiguration implements XcodeIdeBuildConfiguration {
	@Getter private final String name;
	@Getter private final DefaultXcodeIdeBuildSettings buildSettings;
	@Getter private final Property<FileSystemLocation> productLocation;

	public DefaultXcodeIdeBuildConfiguration(String name, ObjectFactory objectFactory) {
		this.name = name;
		this.buildSettings = objectFactory.newInstance(DefaultXcodeIdeBuildSettings.class);
		this.productLocation = configureDisplayName(objectFactory.property(FileSystemLocation.class), "productLocation");
	}

	public void setProductLocation(Object value) {
		ConfigureUtils.setPropertyValue(productLocation, value);
	}
}
