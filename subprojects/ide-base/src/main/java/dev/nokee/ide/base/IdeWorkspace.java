package dev.nokee.ide.base;

import org.gradle.api.Describable;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;

public interface IdeWorkspace<T extends IdeProject> extends Describable {
	Provider<FileSystemLocation> getLocation();
	SetProperty<T> getProjects();
}
