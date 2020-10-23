package dev.nokee.ide.xcode.internal;

import dev.nokee.ide.xcode.XcodeIdeBuildConfiguration;
import lombok.Getter;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import static dev.nokee.utils.ConfigureUtils.configureDisplayName;

public class DefaultXcodeIdeBuildConfiguration implements XcodeIdeBuildConfiguration {
	@Getter private final String name;
	@Getter private final DefaultXcodeIdeBuildSettings buildSettings;
	@Getter private final Property<FileSystemLocation> productLocation;

	public DefaultXcodeIdeBuildConfiguration(String name, ObjectFactory objectFactory) {
		this.name = name;
		this.buildSettings = objectFactory.newInstance(DefaultXcodeIdeBuildSettings.class);
		this.productLocation = configureDisplayName(objectFactory.property(FileSystemLocation.class), "productLocation");
	}
}
