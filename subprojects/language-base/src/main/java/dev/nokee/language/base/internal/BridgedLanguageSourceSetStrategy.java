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
package dev.nokee.language.base.internal;

import com.google.common.collect.ImmutableList;
import lombok.val;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.util.PatternFilterable;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;

import static dev.nokee.utils.FileCollectionUtils.sourceDirectories;

final class BridgedLanguageSourceSetStrategy implements LanguageSourceSetStrategy {
	private final SourceDirectorySet sourceSet;
	private final ObjectFactory objectFactory;

	public BridgedLanguageSourceSetStrategy(SourceDirectorySet sourceSet, ObjectFactory objectFactory) {
		this.sourceSet = sourceSet;
		this.objectFactory = objectFactory;
	}

	@Override
	public void convention(Object... paths) {
		if (sourceSet.getSrcDirTrees().isEmpty()) {
			sourceSet.source(asConventional(objectFactory.fileCollection().from(paths)));
		}
	}

	private SourceDirectorySet asConventional(FileCollection paths) {
		val delegate = new FileCollectionAsSourceDirectorySet(paths);
		val result = objectFactory.sourceDirectorySet(nextAdapterName(), "adapting a file collection as convention");
		result.srcDir(new ReturnEmptyListOnReentrantCallable<>(() -> {
			if (sourceSet.getSrcDirTrees().size() == 0) {
				return delegate.call();
			}
			return ImmutableList.of();
		}));
		result.include(delegate);
		return result;
	}

	private static final class ReturnEmptyListOnReentrantCallable<T> implements Callable<Iterable<T>> {
		private final Callable<Iterable<T>> delegate;
		private boolean reentering = false;

		public ReturnEmptyListOnReentrantCallable(Callable<Iterable<T>> delegate) {
			this.delegate = delegate;
		}

		@Override
		public Iterable<T> call() throws Exception {
			if (reentering) {
				return ImmutableList.of();
			}
			reentering = true;
			try {
				return delegate.call();
			} finally {
				reentering = false;
			}
		}
	}

	@Override
	public void from(Object... paths) {
		sourceSet.source(asSourceDirectorySet(objectFactory.fileCollection().from(paths)));
	}

	private SourceDirectorySet asSourceDirectorySet(FileCollection paths) {
		val delegate = new FileCollectionAsSourceDirectorySet(paths);
		val result = objectFactory.sourceDirectorySet(nextAdapterName(), "adapting a file collection");
		result.srcDir(objectFactory.fileCollection().builtBy(paths));
		result.srcDir(delegate);
		result.include(delegate);
		return result;
	}

	@Override
	public void setFrom(Object... paths) {
		sourceSet.setSrcDirs(Collections.emptyList());
		from(paths);
	}

	private static class FileCollectionAsSourceDirectorySet implements Callable<Iterable<File>>, Spec<FileTreeElement> {
		private final FileCollection delegate;

		public FileCollectionAsSourceDirectorySet(FileCollection delegate) {
			this.delegate = delegate;
		}

		@Override
		public Set<File> call() throws Exception {
			return sourceDirectories(delegate).get();
		}

		@Override
		public boolean isSatisfiedBy(FileTreeElement element) {
			val files = delegate.getFiles();
			File file = element.getFile();
			while (file != null) {
				if (files.contains(file)) {
					return true;
				}
				file = file.getParentFile();
			}
			return false;
		}
	}

	private int counter = 0;
	private String nextAdapterName() {
		return "adapter-" + counter++;
	}

	@Override
	public FileCollection getSourceDirectories() {
		return sourceSet.getSourceDirectories();
	}

	@Override
	public PatternFilterable getFilter() {
		return sourceSet.getFilter();
	}

	@Override
	public FileTree getAsFileTree() {
		return sourceSet.getAsFileTree();
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return task -> sourceSet.getBuildDependencies().getDependencies(task);
	}
}
