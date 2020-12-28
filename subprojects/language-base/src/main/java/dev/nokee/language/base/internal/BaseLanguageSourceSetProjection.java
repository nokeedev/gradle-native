package dev.nokee.language.base.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.val;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.file.FileCollectionInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;

import javax.inject.Inject;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class BaseLanguageSourceSetProjection implements LanguageSourceSetProjection {
	private final ConfigurableFileCollection sources;
	private final ConfigurableFileCollection sourceDirectories;
	private final ObjectFactory objectFactory;
	private final PatternSet patternSet = new PatternSet();
	private FileCollection conventionSources;
	private boolean hasValue = false;

	@Inject
	public BaseLanguageSourceSetProjection(ObjectFactory objectFactory) {
		this.sources = objectFactory.fileCollection().from(conventionIfSourceAbsent());
		this.sourceDirectories = objectFactory.fileCollection().from(sourcesDeduction());
		this.objectFactory = objectFactory;
	}

	public void from(Object... paths) {
		hasValue = hasValue || paths.length > 0;
		sources.from(paths);
	}

	public void convention(Object... paths) {
		this.conventionSources = objectFactory.fileCollection().from(paths);
	}

	public FileCollection getSourceDirectories() {
		return sourceDirectories;
	}

	public PatternFilterable getFilter() {
		return patternSet;
	}

	public FileTree getAsFileTree() {
		return sources.getAsFileTree().matching(patternSet);
	}

	public TaskDependency getBuildDependencies() {
		return sources.getBuildDependencies();
	}

	private Callable<List<FileCollection>> conventionIfSourceAbsent() {
		return () -> {
			if (hasValue || conventionSources == null) {
				return Collections.emptyList();
			}
			return ImmutableList.of(conventionSources);
		};
	}

	private Callable<FileCollection> sourcesDeduction() {
		return () -> {
			val files = ImmutableSet.<File>builder();
			((FileCollectionInternal) sources).visitStructure(new DirectoryStructureVisitor(files));
			return objectFactory.fileCollection().from(files.build()).builtBy(sources);
		};
	}
}
