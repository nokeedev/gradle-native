package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.runtime.base.internal.Dimension;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Named;
import org.gradle.util.GUtil;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class NamingScheme {
	private final String baseName;
	private final ComponentName componentName;
	private final ComponentDisplayName displayName;
	private final ConfigurationPrefix configurationPrefix;
	private final Dimensions dimensions;

	/**
	 * Create a naming scheme for the main component with the specified base name.
	 *
	 * @param baseName the default base name to use
	 * @return a {@link NamingScheme} representing the main component.
	 */
	public static NamingScheme asMainComponent(String baseName) {
		return new NamingScheme(baseName, ComponentName.ofMain(), ComponentDisplayName.missing(), ConfigurationPrefix.none(), Dimensions.empty());
	}

	/**
	 * Creates a naming scheme for a component with the specified base name and logic name.
	 *
	 * @param baseName the default base name to use.
	 * @param name the logic name to use, i.e. test, functionalTest.
	 * @return a {@link NamingScheme} representing the component.
	 */
	public static NamingScheme asComponent(String baseName, String name) {
		return new NamingScheme(baseName, ComponentName.of(name), ComponentDisplayName.missing(), ConfigurationPrefix.none(), Dimensions.empty());
	}

	/**
	 * Returns the configuration name for the target using this scheme, i.e. implementation, testImplementation, testWindowsImplementation, testWindowsNativeImplementation, jvmImplementation.
	 *
	 * @param target a configuration target, i.e. implementation, compileOnly
	 * @return a fully formed configuration name, never null.
	 */
	public final String getConfigurationName(String target) {
		return componentName.prefix(dimensions.prefix(configurationPrefix.prefix(target)));
	}

	/**
	 * Returns the configuration name for the target using this scheme without the configuration prefix, i.e. implementation, testImplementation, testWindowsImplementation.
	 * The configuration prefix is used by JNI to differentiate native and jvm.
	 *
	 * @param target a configuration target, i.e. implementation, compileOnly
	 * @return a fully formed configuration name, never null.
	 */
	public final String getConfigurationNameWithoutPrefix(String target) {
		return componentName.prefix(dimensions.prefix(target));
	}

	public String getComponentName() {
		return componentName.get();
	}

	public BaseNameNamingScheme getBaseName() {
		return new BaseNameNamingScheme();
	}

	public String getResourcePath(GroupId groupId) {
		return groupId.get().map(it -> it.replace('.', '/') + '/').orElse("") + dimensions.getAsKebab().orElse("");
	}

	/**
	 * Creates a copy of this scheme, <em>adding</em> a variant dimension if required.
	 */
	public <T extends Named> NamingScheme withVariantDimension(T value, Collection<? extends T> allValuesForAxis) {
		if (allValuesForAxis.size() == 1) {
			return this;
		}

		return new NamingScheme(baseName, componentName, displayName, configurationPrefix, dimensions.add(value.getName()));
	}

	public NamingScheme forBuildVariant(BuildVariant value, Collection<? extends BuildVariant> allBuildVariants) {
		NamingScheme result = this;
		int index = 0;
		for (Dimension dimension : value.getDimensions()) {
			if (dimension instanceof Named) {
				Set<Named> allValuesForAxis = allBuildVariants.stream().map(extractDimensionAtIndex(index)).collect(Collectors.toSet());
				result = result.withVariantDimension((Named)dimension, allValuesForAxis);
			} else {
				throw new IllegalArgumentException("The dimension needs to implement Named, it's an implementation detail at this point");
			}
			index++;
		}

		return result;
	}

	private Function<BuildVariant, Named> extractDimensionAtIndex(int index) {
		return buildVariant -> (Named)buildVariant.getDimensions().get(index);
	}

	/**
	 * Returns the task name for the specified verb, i.e. link, linkWindows.
	 *
	 * @param verb a task action prefix, i.e. link
	 * @return a fully formed task name, never null.
	 */
	public String getTaskName(String verb) {
		return dimensions.suffix(componentName.suffix(verb));
	}

	/**
	 * Returns the task name for the specified verb and object, i.e. compileCpp, compileWindowsCpp.
	 *
	 * @param verb a task action prefix, i.e. compile
	 * @param object a task target suffix, i.e. cpp, swift
	 * @return a fully formed task name, never null.
	 */
	public String getTaskName(String verb, String object) {
		return getTaskName(verb) + StringUtils.capitalize(object);
	}

	public String getOutputDirectoryBase(String outputType) {
		return outputType
			+ prefixWithPathSeparator(componentName.get())
			+ dimensions.get().map(NamingScheme::prefixWithPathSeparator).orElse("");
	}

	private static String prefixWithPathSeparator(String value) {
		return "/" + value;
	}

    public NamingScheme withConfigurationNamePrefix(String configurationNamePrefix) {
		return new NamingScheme(baseName, componentName, displayName, ConfigurationPrefix.of(configurationNamePrefix), dimensions);
	}

	public NamingScheme withComponentDisplayName(String componentDisplayName) {
		return new NamingScheme(baseName, componentName, ComponentDisplayName.of(componentDisplayName), configurationPrefix, dimensions);
	}

	/**
	 * Returns completed description with the display name.
	 * @param format a string format pattern with a '%s' where the display name should be placed.
	 * @return a fully formed description, never null.
	 * @throws IllegalStateException if no component display name were configured on this naming scheme.
	 */
	public String getConfigurationDescription(String format) {
		return String.format(format, displayName.get());
	}

	public class BaseNameNamingScheme {
		private BaseNameNamingScheme() {}

		public String getAsString() {
			return baseName;
		}

		public String getAsCamelCase() {
			return GUtil.toCamelCase(baseName);
		}

		public String withKababDimensions() {
			List<String> values = new ArrayList<>();
			values.add(baseName);
			dimensions.getAsKebab().ifPresent(values::add);
			return String.join("-", values);
		}
    }

    public interface Prefixer {
		String prefix(String target);
	}

	public interface Suffixer {
		String suffix(String target);
	}

	@RequiredArgsConstructor
	public static abstract class BaseValueNamingSegment {
		protected final String value;

		public String prefix(String target) {
			return value + StringUtils.capitalize(target);
		}

		public String suffix(String target) {
			return target + StringUtils.capitalize(value);
		}
	}

	public static abstract class BaseNoopNamingSegment {
		public String prefix(String target) {
			return target;
		}

		public String suffix(String target) {
			return target;
		}
	}

	//region ConfigurationPrefixer
	public interface ConfigurationPrefix extends Prefixer {
		static ConfigurationPrefix none() {
			return AbsentConfigurationPrefix.INSTANCE;
		}

		static ConfigurationPrefix of(String value) {
			return new DefaultConfigurationPrefix(value);
		}
	}

	private static class AbsentConfigurationPrefix extends BaseNoopNamingSegment implements ConfigurationPrefix {
		private static final AbsentConfigurationPrefix INSTANCE = new AbsentConfigurationPrefix();
	}

	private static class DefaultConfigurationPrefix extends BaseValueNamingSegment implements ConfigurationPrefix {
		public DefaultConfigurationPrefix(String value) {
			super(value);
		}
	}
	//endregion

	//region Dimensions
	public interface Dimensions extends Prefixer, Suffixer {
		Dimensions add(String dimension);

		// TODO: Replace with the correct naming segment operation
		List<String> getDimensions();

		Optional<String> get();

		Optional<String> getAsKebab();

		static Dimensions empty() {
			return NoDimensions.INSTANCE;
		}
	}

	public static class NoDimensions extends BaseNoopNamingSegment implements Dimensions {
		public static final NoDimensions INSTANCE = new NoDimensions();

		@Override
		public Dimensions add(String dimension) {
			return new WithDimensions(ImmutableList.of(dimension));
		}

		@Override
		public List<String> getDimensions() {
			return ImmutableList.of();
		}

		@Override
		public Optional<String> get() {
			return Optional.empty();
		}

		@Override
		public Optional<String> getAsKebab() {
			return Optional.empty();
		}
	}

	public static class WithDimensions extends BaseValueNamingSegment implements Dimensions {
		private final List<String> dimensions;

		public WithDimensions(List<String> dimensions) {
			super(createPrefix(dimensions));
			this.dimensions = dimensions;
		}

		@Override
		public Dimensions add(String dimension) {
			return new WithDimensions(ImmutableList.<String>builder().addAll(dimensions).add(dimension).build());
		}

		@Override
		public List<String> getDimensions() {
			return dimensions;
		}

		private static String createPrefix(List<String> dimensions) {
			return StringUtils.uncapitalize(dimensions.stream().map(StringUtils::capitalize).collect(Collectors.joining()));
		}

		@Override
		public Optional<String> get() {
			return Optional.of(value);
		}

		@Override
		public Optional<String> getAsKebab() {
			return Optional.of(String.join("-", dimensions));
		}
	}
	//endregion

	//region ComponentName
	public interface ComponentName extends Prefixer, Suffixer {
		String get();

		static ComponentName ofMain() {
			return new MainComponentName();
		}

		static ComponentName of(String name) {
			return new OtherComponentName(name);
		}
	}

	public static class MainComponentName extends BaseNoopNamingSegment implements ComponentName {
		@Override
		public String get() {
			return "main";
		}
	}

	public static class OtherComponentName extends BaseValueNamingSegment implements ComponentName {
		public OtherComponentName(String value) {
			super(value);
		}

		@Override
		public String get() {
			return value;
		}
	}
	//endregion

	//region ComponentDisplayName
    public interface ComponentDisplayName{
		String get();

		static ComponentDisplayName missing() {
			return MissingComponentDisplayName.INSTANCE;
		}

		static ComponentDisplayName of(String value) {
			return new DefaultComponentDisplayName(value);
		}
	}

	private static class MissingComponentDisplayName implements ComponentDisplayName {
		private static final MissingComponentDisplayName INSTANCE = new MissingComponentDisplayName();

		public String get() {
			throw new IllegalStateException("make sure component display name is set");
		}
	}

	@RequiredArgsConstructor
	private static class DefaultComponentDisplayName implements ComponentDisplayName {
		private final String displayName;

		public String get() {
			return displayName;
		}
	}
	//endregion
}
