package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represent a collection of dimension values.
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class Dimensions {
	public abstract Dimensions add(String dimension);

	@EqualsAndHashCode.Include
	public abstract List<String> get();

	public abstract Optional<String> getAsKebabCase();

	public abstract Optional<String> getAsCamelCase();

	public abstract Optional<String> getAsLowerCamelCase();

	public abstract int size();

	public static Dimensions empty() {
		return NoDimensions.INSTANCE;
	}

	public static Dimensions of(List<String> dimensions) {
		if (dimensions.isEmpty()) {
			return NoDimensions.INSTANCE;
		}
		return new WithDimensions(ImmutableList.copyOf(dimensions));
	}

	public static final class WithDimensions extends Dimensions {
		private final List<String> dimensions;

		public WithDimensions(List<String> dimensions) {
			this.dimensions = dimensions;
		}

		@Override
		public Dimensions add(String dimension) {
			return new WithDimensions(ImmutableList.<String>builder().addAll(dimensions).add(dimension).build());
		}

		@Override
		public List<String> get() {
			return dimensions;
		}

		@Override
		public Optional<String> getAsKebabCase() {
			return Optional.of(String.join("-", dimensions));
		}

		@Override
		public Optional<String> getAsCamelCase() {
			return Optional.of(dimensions.stream().map(StringUtils::capitalize).collect(Collectors.joining()));
		}

		@Override
		public Optional<String> getAsLowerCamelCase() {
			return getAsCamelCase().map(StringUtils::uncapitalize);
		}

		@Override
		public int size() {
			return dimensions.size();
		}
	}

	private static final class NoDimensions extends Dimensions {
		public static final NoDimensions INSTANCE = new NoDimensions();

		@Override
		public Dimensions add(String dimension) {
			return new WithDimensions(ImmutableList.of(dimension));
		}

		@Override
		public List<String> get() {
			return ImmutableList.of();
		}

		@Override
		public Optional<String> getAsKebabCase() {
			return Optional.empty();
		}

		@Override
		public Optional<String> getAsCamelCase() {
			return Optional.empty();
		}

		@Override
		public Optional<String> getAsLowerCamelCase() {
			return Optional.empty();
		}

		@Override
		public int size() {
			return 0;
		}
	}
}
