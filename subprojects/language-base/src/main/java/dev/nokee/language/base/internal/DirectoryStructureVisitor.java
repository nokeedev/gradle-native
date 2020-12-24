package dev.nokee.language.base.internal;

import com.google.common.collect.ImmutableSet;
import org.gradle.api.internal.file.FileCollectionInternal;
import org.gradle.api.internal.file.FileCollectionStructureVisitor;
import org.gradle.api.internal.file.FileTreeInternal;
import org.gradle.api.tasks.util.PatternSet;

import java.io.File;

final class DirectoryStructureVisitor implements FileCollectionStructureVisitor {
	private final ImmutableSet.Builder<File> result;

	public DirectoryStructureVisitor(ImmutableSet.Builder<File> result) {
		this.result = result;
	}

	@Override
	public void visitCollection(FileCollectionInternal.Source source, Iterable<File> iterable) {
		for (File file : iterable) {
			result.add(toSourceDirectory(file));
		}
	}

	@Override
	public void visitGenericFileTree(FileTreeInternal fileTreeInternal) {
		throw new UnsupportedOperationException("Unaccounted use case, please open an issue for source directories deduction out of generic file tree");
	}

	@Override
	public void visitFileTree(File file, PatternSet patternSet, FileTreeInternal fileTreeInternal) {
		result.add(toSourceDirectory(file));
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
