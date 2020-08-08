package dev.nokee.platform.base.internal.dependencies;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public abstract class ComponentNamingScheme implements PrefixingNamingScheme {
	private static final String MAIN_COMPONENT_NAME = "main";

	public abstract String prefix(String target);

	public static ComponentNamingScheme ofMain() {
		return MainComponentNamingScheme.INSTANCE;
	}

	public static ComponentNamingScheme of(String name) {
		if (MAIN_COMPONENT_NAME.equals(name)) {
			return MainComponentNamingScheme.INSTANCE;
		}
		return new OtherComponentNamingScheme(Objects.requireNonNull(name));
	}

	private static class MainComponentNamingScheme extends ComponentNamingScheme {
		private static final MainComponentNamingScheme INSTANCE = new MainComponentNamingScheme();
		@Override
		public String prefix(String target) {
			return target;
		}
	}

	private static class OtherComponentNamingScheme extends ComponentNamingScheme {
		private final String name;

		public OtherComponentNamingScheme(String name) {
			this.name = name;
		}

		@Override
		public String prefix(String target) {
			return name + StringUtils.capitalize(target);
		}
	}
}
