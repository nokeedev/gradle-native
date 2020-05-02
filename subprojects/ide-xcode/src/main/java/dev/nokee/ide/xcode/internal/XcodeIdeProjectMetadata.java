package dev.nokee.ide.xcode.internal;

import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.internal.Describables;
import org.gradle.internal.DisplayName;
import org.gradle.plugins.ide.internal.IdeProjectMetadata;

import java.io.File;
import java.util.Collections;
import java.util.Set;

public class XcodeIdeProjectMetadata implements IdeProjectMetadata {
    private final Provider<DefaultXcodeIdeProject> xcodeProject;

    public XcodeIdeProjectMetadata(Provider<DefaultXcodeIdeProject> xcodeProject) {
        this.xcodeProject = xcodeProject;
    }

    @Override
    public DisplayName getDisplayName() {
        return Describables.withTypeAndName("Xcode project", xcodeProject.get().getName());
    }

    @Override
    public Set<? extends Task> getGeneratorTasks() {
        return Collections.singleton(xcodeProject.get().getGeneratorTask().get());
    }

    @Override
    public File getFile() {
        return xcodeProject.get().getLocation().get().getAsFile();
    }
}
