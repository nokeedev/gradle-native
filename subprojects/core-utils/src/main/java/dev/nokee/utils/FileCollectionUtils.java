/*
 * Copyright 2021 the original author or authors.
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
import lombok.val;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.internal.file.FileCollectionInternal;
import org.gradle.api.provider.Provider;

import java.io.File;
import java.util.Set;
import java.util.function.Function;

public final class FileCollectionUtils {
	/**
	 * Returns the element provider of a file collection by mapping the input object into the file collection first.
	 *
	 * @param mapper  the object to {@link FileCollection} mapper, must not be null
	 * @param <T>  the object type
	 * @return a transformer for object to {@literal FileCollection} to {@literal FileSystemLocation} set provider, never null
	 */
	public static <T> TransformerUtils.Transformer<Provider<Set<FileSystemLocation>>, T> elementsOf(Function<? super T, ? extends FileCollection> mapper) {
		return new ElementsOfTransformer<>(mapper);
	}

	/** @see #elementsOf(Function) */
	private static final class ElementsOfTransformer<T> implements TransformerUtils.Transformer<Provider<Set<FileSystemLocation>>, T> {
		private final Function<? super T, ? extends FileCollection> mapper;

		private ElementsOfTransformer(Function<? super T, ? extends FileCollection> mapper) {
			this.mapper = mapper;
		}

		@Override
		public Provider<Set<FileSystemLocation>> transform(T t) {
			return mapper.apply(t).getElements();
		}

		@Override
		public String toString() {
			return "FileCollectionUtils.elementsOf(" + mapper + ")";
		}
	}

	public static Provider<Set<File>> sourceDirectories(FileCollection self) {
		return self.getElements().map(__ -> {
			val files = ImmutableSet.<File>builder();
			((FileCollectionInternal) self).visitStructure(new DirectoryStructureVisitor(files));
			return files.build();
		});
	}
}
