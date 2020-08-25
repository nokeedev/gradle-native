package dev.nokee.language.nativebase.internal;

import dev.nokee.language.base.internal.UTType;
import dev.nokee.language.base.tasks.SourceCompile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileTree;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

@AllArgsConstructor
public final class ObjectSourceSetInternal {
	@Getter private final String name;
	@Getter private final UTType type;
	@Getter private final Provider<Directory> objectDirectory;
	@Getter private final TaskProvider<? extends SourceCompile> generatedByTask;

	public FileTree getAsFileTree() {
		return objectDirectory.get().getAsFileTree();
	}
}
