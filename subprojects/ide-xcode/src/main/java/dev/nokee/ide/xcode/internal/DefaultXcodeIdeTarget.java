package dev.nokee.ide.xcode.internal;

import dev.nokee.ide.xcode.XcodeIdeBuildConfiguration;
import dev.nokee.ide.xcode.XcodeIdeProductType;
import dev.nokee.ide.xcode.XcodeIdeTarget;
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
