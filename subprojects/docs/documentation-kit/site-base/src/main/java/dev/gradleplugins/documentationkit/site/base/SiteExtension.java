package dev.gradleplugins.documentationkit.site.base;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

public interface SiteExtension {
	Property<String> getHost();
	ConfigurableFileCollection getSources();
	DirectoryProperty getDestinationDirectory();
	MapProperty<String, String> getSymbolicLinks();
}
