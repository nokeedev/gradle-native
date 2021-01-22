package dev.gradleplugins.documentationkit.rendering.jbake;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;

public interface JBakeExtension {
	ConfigurableFileCollection getClasspath();
	ConfigurableFileCollection getContent();
	ConfigurableFileCollection getAssets();
	ConfigurableFileCollection getTemplates();
	RegularFileProperty getPropertiesFile();
	DirectoryProperty getDestinationDirectory();
}
