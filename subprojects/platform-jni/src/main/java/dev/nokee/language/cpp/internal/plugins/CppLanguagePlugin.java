package dev.nokee.language.cpp.internal.plugins;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.AppliedPlugin;

public class CppLanguagePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("cpp");
        project.getPluginManager().withPlugin("dev.nokee.jni-library", applyCppJniLibraryRules(project));
    }

    private Action<? super AppliedPlugin> applyCppJniLibraryRules(Project project) {
        return appliedPlugin -> {
            project.getPluginManager().apply(CppJniLibraryRules.class);
        };
    }
}
