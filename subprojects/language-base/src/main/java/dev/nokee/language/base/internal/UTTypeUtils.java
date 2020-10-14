package dev.nokee.language.base.internal;

import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Operations on {@link UTType} for build script authoring.
 *
 * @since 0.4
 */
public class UTTypeUtils {
	/**
	 * Creates a list of filters for all extensions of a {@link UTType}.
	 *
	 * @param type a uniform type to create the list of filters
	 * @return a list of filters for all elements returned by {@link UTType#getFilenameExtensions()}, never null.
	 */
	public static PatternFilterable onlyIf(UTType type) {
		return new PatternSet().include(Arrays.stream(type.getFilenameExtensions()).map(it -> "**/*." + it).collect(Collectors.toList()));
	}
}
