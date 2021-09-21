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
import dev.nokee.ide.xcode.XcodeIdeProductType;
import dev.nokee.ide.xcode.XcodeIdeTarget;
import dev.nokee.utils.ConfigureUtils;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import static dev.nokee.utils.ConfigureUtils.configureDisplayName;

public final class DefaultXcodeIdeTarget implements XcodeIdeTarget {
	@Getter private final String name;
	@Getter private final Property<String> productName;
	@Getter private final Property<XcodeIdeProductType> productType;
	@Getter private final Property<String> productReference;
	@Getter private final ConfigurableFileCollection sources;
	@Getter private final NamedDomainObjectContainer<XcodeIdeBuildConfiguration> buildConfigurations;
	private final ObjectFactory objectFactory;

	public DefaultXcodeIdeTarget(String name, ObjectFactory objectFactory) {
		this.name = name;
		this.productName = configureDisplayName(objectFactory.property(String.class), "productName");
		this.productType = configureDisplayName(objectFactory.property(XcodeIdeProductType.class), "productType");
		this.productReference = configureDisplayName(objectFactory.property(String.class), "productReference");
		this.sources = objectFactory.fileCollection();
		this.buildConfigurations = objectFactory.domainObjectContainer(XcodeIdeBuildConfiguration.class, this::newBuildConfiguration);
		this.objectFactory = objectFactory;
		getProductName().convention(getProductReference().map(this::toProductName));
	}

	// TODO: Use product type to better guide the conversion
	private String toProductName(String filename) {
		String result = FilenameUtils.removeExtension(filename);
		if (filename.startsWith("lib")) {
			result = result.substring(3);
		}
		return result;
	}

	public void setProductName(Object value) {
		ConfigureUtils.setPropertyValue(productName, value);
	}

	public void setProductType(Object value) {
		ConfigureUtils.setPropertyValue(productType, value);
	}

	public void setProductReference(Object value) {
		ConfigureUtils.setPropertyValue(productReference, value);
	}

	@Override
	public void buildConfigurations(@NonNull Action<? super NamedDomainObjectContainer<XcodeIdeBuildConfiguration>> action) {
		action.execute(buildConfigurations);
	}

	private XcodeIdeBuildConfiguration newBuildConfiguration(String name) {
		return new DefaultXcodeIdeBuildConfiguration(name, objectFactory);
	}

	@Override
	public XcodeIdeTarget getIdeTarget() {
		return this;
	}
}
