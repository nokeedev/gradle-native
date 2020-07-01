package dev.nokee.ide.base;

import org.gradle.api.Describable;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;

public interface IdeWorkspace extends Describable {
	Provider<FileSystemLocation> getLocation();

}
