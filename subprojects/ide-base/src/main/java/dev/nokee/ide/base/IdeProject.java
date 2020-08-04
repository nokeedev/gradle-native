package dev.nokee.ide.base;

import org.gradle.api.Named;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;

public interface IdeProject extends Named, IdeProjectReference {
	Provider<FileSystemLocation> getLocation();

}
