package dev.nokee.ide.xcode.internal;

import dev.nokee.ide.base.internal.BaseIdeProjectReference;
import dev.nokee.ide.xcode.XcodeIdeProjectReference;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.InputDirectory;

public class DefaultXcodeIdeProjectReference extends BaseIdeProjectReference implements XcodeIdeProjectReference {
    public DefaultXcodeIdeProjectReference(Provider<DefaultXcodeIdeProject> xcodeProject) {
        super(xcodeProject);
    }

	@InputDirectory
	@Override
	public Provider<FileSystemLocation> getLocation() {
		return super.getLocation();
	}
}
