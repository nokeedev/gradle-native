package dev.nokee.language.base.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.language.base.LanguageSourceSet;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Buildable;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.file.FileCollectionInternal;
import org.gradle.api.internal.file.FileCollectionStructureVisitor;
import org.gradle.api.internal.file.FileTreeInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public abstract class AbstractLanguageSourceSet<SELF extends LanguageSourceSet> implements LanguageSourceSetInternal, Buildable {
	private final LanguageSourceSetIdentifier<?> identifier;
	private final Class<SELF> publicType;
	private final ConfigurableFileCollection sources;
	private final ConfigurableFileCollection sourceDirectories;
	private final ObjectFactory objectFactory;
	private final PatternSet patternSet = new PatternSet();
	private FileCollection conventionSources;
	private boolean hasValue = false;

	protected AbstractLanguageSourceSet(LanguageSourceSetIdentifier<?> identifier, Class<SELF> publicType, ObjectFactory objectFactory) {
		this.identifier = identifier;
		this.publicType = publicType;
		this.sources = objectFactory.fileCollection().from(conventionIfSourceAbsent());
		this.sourceDirectories = objectFactory.fileCollection().from(sourcesDeduction());
		this.objectFactory = objectFactory;
	}

	@Override
	public LanguageSourceSetIdentifier<?> getIdentifier() {
		return identifier;
	}

	@Override
	public SELF from(Object... paths) {
		hasValue = hasValue || paths.length > 0;
		sources.from(paths);
		return publicType.cast(this);
	}

	@Override
	public SELF convention(FileCollection files) {
		this.conventionSources = files;
		return publicType.cast(this);
	}

	@Override
	public FileCollection getSourceDirectories() {
		return sourceDirectories;
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
			val result = objectFactory.fileCollection();
			((FileCollectionInternal) sources).visitStructure(new DirectoryStructureVisitor(result));
			result.builtBy(sources);
			return result;
		};
	}

	private static final class DirectoryStructureVisitor implements FileCollectionStructureVisitor {
		private final ConfigurableFileCollection result;

		DirectoryStructureVisitor(ConfigurableFileCollection result) {
			this.result = result;
		}

		@Override
		public void visitCollection(FileCollectionInternal.Source source, Iterable<File> iterable) {
			for (File file : iterable) {
				result.from(toSourceDirectory(file));
			}
		}

		@Override
		public void visitGenericFileTree(FileTreeInternal fileTreeInternal) {
			throw new UnsupportedOperationException("Unaccounted use case, please open an issue for source directories deduction out of generic file tree");
		}

		@Override
		public void visitFileTree(File file, PatternSet patternSet, FileTreeInternal fileTreeInternal) {
			result.from(file);
		}

		@Override
		public void visitFileTreeBackedByFile(File file, FileTreeInternal fileTreeInternal) {
			throw new UnsupportedOperationException("Unaccounted use case, please open an issue for source directories deduction out of file tree backed by file");
		}

		private static File toSourceDirectory(File file) {
			if (file.isDirectory()) {
				return file;
			} else if (nonExistingFileAssumeSourceDirectory(file)) {
				return file;
			}
			return file.getParentFile();
		}

		private static boolean nonExistingFileAssumeSourceDirectory(File file) {
			// This allows to keep non existing directory.
			// We keep non-existing directory because some code around header directories assume non-existing source directories to show up.
			// It may help users "see" the directory is accounted for event if it's non-existent.
			// We may want to ignore non-existent directory this in the future... will see.
			return !file.exists();
		}
	}
}
