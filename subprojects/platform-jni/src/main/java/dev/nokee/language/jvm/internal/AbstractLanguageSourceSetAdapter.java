package dev.nokee.language.jvm.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Buildable;
import org.gradle.api.file.*;
import org.gradle.api.internal.file.FileCollectionInternal;
import org.gradle.api.internal.file.FileCollectionStructureVisitor;
import org.gradle.api.internal.file.FileTreeInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractLanguageSourceSetAdapter<SELF extends LanguageSourceSet> implements LanguageSourceSetInternal, Buildable {
	private final LanguageSourceSetIdentifier<?> identifier;
	private final Class<SELF> publicType;
	private final SourceDirectorySet sourceSet;
	private final ObjectFactory objectFactory;
	private FileCollection conventionSources;
	private final ConfigurableFileCollection conventionSourceDirectories;

	protected AbstractLanguageSourceSetAdapter(LanguageSourceSetIdentifier<?> identifier, Class<SELF> publicType, SourceDirectorySet sourceSet, ObjectFactory objectFactory) {
		this.identifier = identifier;
		this.publicType = publicType;
		this.sourceSet = sourceSet;
		this.objectFactory = objectFactory;
		sourceSet.setSrcDirs(ImmutableList.of());
		this.conventionSourceDirectories = objectFactory.fileCollection();
	}

	@Override
	public LanguageSourceSetIdentifier<?> getIdentifier() {
		return identifier;
	}

	@Override
	public SELF convention(FileCollection files) {
		this.conventionSources = files;
		conventionSourceDirectories.setFrom(sourcesDeduction(files));
		return publicType.cast(this);
	}

	private FileCollection sourcesDeduction(FileCollection sources) {
		val result = objectFactory.fileCollection();
		((FileCollectionInternal) sources).visitStructure(new DirectoryStructureVisitor(result));
		result.builtBy(sources);
		return result;
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


	@Override
	public SELF from(Object... paths) {
		for (Object path : paths) {
			if (path instanceof File && ((File) path).isFile()) {
				sourceSet.srcDir(((File) path).getParentFile());
				sourceSet.include(((File) path).getName());
			} else if (path instanceof ConfigurableFileTree) {
				sourceSet.srcDir((Callable<File>) ((ConfigurableFileTree) path)::getDir);
				sourceSet.include(element -> ((FileTree) path).getFiles().contains(element.getFile()));
			} else if (path instanceof FileCollection) {
				sourceSet.srcDir(((FileCollection) path).getElements().map(files -> {
					return files.stream().flatMap(location -> {
						val file = location.getAsFile();
						if (file.isFile()) {
							return Stream.of(file.getParentFile());
						}
						return Stream.of(file);
					}).collect(Collectors.toList());
				}));
				sourceSet.getFilter().include(element -> ((ConfigurableFileCollection) path).getFiles().contains(element.getFile()));
			} else {
				sourceSet.srcDir(path);
			}
		}
		return publicType.cast(this);
	}

	@Override
	public FileCollection getSourceDirectories() {
		if (sourceSet.getSourceDirectories().isEmpty()) {
			return conventionSourceDirectories;
		}
		return sourceSet.getSourceDirectories();
	}

	@Override
	public SELF filter(Action<? super PatternFilterable> action) {
		action.execute(sourceSet.getFilter());
		return publicType.cast(this);
	}

	@Override
	public PatternFilterable getFilter() {
		return sourceSet.getFilter();
	}

	@Override
	public FileTree getAsFileTree() {
		if (sourceSet.isEmpty() && conventionSources != null) {
			return conventionSources.getAsFileTree().matching(getFilter());
		}
		return sourceSet.getAsFileTree();
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return task -> sourceSet.getSourceDirectories().getBuildDependencies().getDependencies(task);
	}
}
