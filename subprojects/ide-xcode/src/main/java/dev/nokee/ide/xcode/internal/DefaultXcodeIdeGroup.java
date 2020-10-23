package dev.nokee.ide.xcode.internal;

import dev.nokee.ide.xcode.XcodeIdeGroup;
import lombok.Getter;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public final class DefaultXcodeIdeGroup implements XcodeIdeGroup {
	@Getter private final String name;
	@Getter private final ConfigurableFileCollection sources;

	@Inject
	public DefaultXcodeIdeGroup(String name, ObjectFactory objectFactory) {
		this.name = name;
		this.sources = objectFactory.fileCollection();
	}
}
