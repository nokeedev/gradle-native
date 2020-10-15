package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.DomainObjectIdentifier;
import org.gradle.api.Action;
import org.gradle.api.Buildable;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileTree;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.util.Configurable;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractLanguageSourceSet<SELF extends LanguageSourceSet> implements LanguageSourceSet, Buildable {
	private final Class<SELF> publicType;
	private final ConfigurableFileCollection sources;
	private final ConfigurableFileCollection sourceDirectories;
	private final PatternSet patternSet = new PatternSet();

	protected AbstractLanguageSourceSet(Class<SELF> publicType, ObjectFactory objects) {
		this.publicType = publicType;
		this.sources = objects.fileCollection();
		this.sourceDirectories = objects.fileCollection().from(sources.getElements().map(this::toSourceDirectories));
	}

	@Override
	public SELF from(Object... paths) {
		sources.from(paths);
		return publicType.cast(this);
	}

	@Override
	public FileCollection getSourceDirectories() {
		return sourceDirectories;
	}

	private Iterable<File> toSourceDirectories(Set<FileSystemLocation> files) {
		return files.stream().map(FileSystemLocation::getAsFile).map(this::toSourceDirectory).collect(Collectors.toList());
	}

	private File toSourceDirectory(File file) {
		if (file.isDirectory()) {
			return file;
		}
		return file.getParentFile();
	}

	@Override
	public PatternFilterable getFilter() {
		return patternSet;
	}

	@Override
	public SELF filter(Action<? super PatternFilterable> action) {
		action.execute(patternSet);
		return publicType.cast(this);
	}

	@Override
	public FileTree getAsFileTree() {
		return sources.getAsFileTree().matching(patternSet);
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return sources.getBuildDependencies();
	}
}
