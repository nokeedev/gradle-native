package dev.nokee.language.nativebase.internal.toolchains;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class NokeeStandardToolChainsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(NokeeMicrosoftVisualCppCompilerPlugin.class);
        project.getPluginManager().apply(NokeeGccCompilerPlugin.class);
        project.getPluginManager().apply(NokeeClangCompilerPlugin.class);
    }
}
