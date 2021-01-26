package dev.gradleplugins.documentationkit;

import dev.nokee.language.base.LanguageSourceSet;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;

import java.net.URI;

public interface JavadocApiReference {
	LanguageSourceSet getSources();
	Property<String> getPermalink();
	Property<String> getTitle();
	DirectoryProperty getDestinationDirectory();
	SetProperty<URI> getLinks();
	ConfigurableFileCollection getClasspath();

}
