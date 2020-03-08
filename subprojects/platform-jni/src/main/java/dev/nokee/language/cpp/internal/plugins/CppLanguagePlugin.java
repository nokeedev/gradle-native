package dev.nokee.language.cpp.internal.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class CppLanguagePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("cpp");
    }
}
