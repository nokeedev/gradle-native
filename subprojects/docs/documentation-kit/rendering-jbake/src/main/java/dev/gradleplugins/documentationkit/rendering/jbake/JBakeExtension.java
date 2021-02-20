package dev.gradleplugins.documentationkit.rendering.jbake;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.MapProperty;

public interface JBakeExtension {
	ConfigurableFileCollection getClasspath();
	ConfigurableFileCollection getContent();
	ConfigurableFileCollection getAssets();
	ConfigurableFileCollection getTemplates();
	MapProperty<String, Object> getConfigurations();
	DirectoryProperty getDestinationDirectory();
}
