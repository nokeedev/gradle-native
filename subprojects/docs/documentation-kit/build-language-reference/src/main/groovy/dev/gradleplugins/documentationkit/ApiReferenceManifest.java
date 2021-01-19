package dev.gradleplugins.documentationkit;

import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.SetProperty;

import java.net.URI;

public interface ApiReferenceManifest {
	ConfigurableFileCollection getSources();
	SetProperty<Dependency> getDependencies();
	SetProperty<URI> getRepositories();
	DirectoryProperty getDestinationLocation();
}
