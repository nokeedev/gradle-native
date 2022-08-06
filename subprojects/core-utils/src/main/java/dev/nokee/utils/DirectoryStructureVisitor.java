/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.utils;

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
