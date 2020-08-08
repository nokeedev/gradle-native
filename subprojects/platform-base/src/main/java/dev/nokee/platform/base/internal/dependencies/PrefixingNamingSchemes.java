package dev.nokee.platform.base.internal.dependencies;

import org.apache.commons.lang3.StringUtils;

public final class PrefixingNamingSchemes  {
	public static PrefixingNamingScheme of(String prefix) {
		if (prefix.isEmpty()) {
			return NoopPrefixingNamingScheme.INSTANCE;
		}
		return new DefaultPrefixingNamingScheme(prefix);
	}

	private static class NoopPrefixingNamingScheme implements PrefixingNamingScheme {
		public static final NoopPrefixingNamingScheme INSTANCE = new NoopPrefixingNamingScheme();

		@Override
		public String prefix(String target) {
			return target;
		}
	}

	private static class DefaultPrefixingNamingScheme implements PrefixingNamingScheme {
		private final String prefix;

		public  DefaultPrefixingNamingScheme(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public String prefix(String target) {
			return prefix + StringUtils.capitalize(target);
		}
	}
}
