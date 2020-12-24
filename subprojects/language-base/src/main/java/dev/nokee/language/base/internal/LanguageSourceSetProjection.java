package dev.nokee.language.base.internal;

import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.util.PatternFilterable;

public interface LanguageSourceSetProjection {
	void from(Object... paths);
	void convention(Object... paths);
	FileCollection getSourceDirectories();
	PatternFilterable getFilter();
	FileTree getAsFileTree();
	TaskDependency getBuildDependencies();
}
