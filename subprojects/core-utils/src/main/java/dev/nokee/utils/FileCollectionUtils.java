package dev.nokee.utils;

import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;

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
}
