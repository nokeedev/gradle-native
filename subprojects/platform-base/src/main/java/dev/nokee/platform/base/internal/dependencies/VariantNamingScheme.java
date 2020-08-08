package dev.nokee.platform.base.internal.dependencies;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class VariantNamingScheme implements PrefixingNamingScheme {
	public abstract String prefix(String target);

	public static VariantNamingScheme of() {
		return new NoDimensionVariantNamingScheme();
	}

	public static VariantNamingScheme of(String... dimensions) {
		return of(Arrays.asList(dimensions));
	}

	public static VariantNamingScheme of(List<String> dimensions) {
		if (dimensions.isEmpty()) {
			return new NoDimensionVariantNamingScheme();
		}
		return new DefaultVariantNamingScheme(createPrefix(dimensions));
	}

	private static String createPrefix(List<String> dimensions) {
		return StringUtils.uncapitalize(dimensions.stream().map(StringUtils::capitalize).collect(Collectors.joining()));
	}

	private static class NoDimensionVariantNamingScheme extends VariantNamingScheme {
		@Override
		public String prefix(String target) {
			return target;
		}
	}

	private static class DefaultVariantNamingScheme extends VariantNamingScheme {
		private final String prefix;

		public DefaultVariantNamingScheme(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public String prefix(String target) {
			return prefix + StringUtils.capitalize(target);
		}
	}
}
