package dev.nokee.platform.base.internal;

import com.google.common.base.Preconditions;
import dev.nokee.runtime.base.internal.Dimension;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Named;
import org.gradle.util.GUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public abstract class NamingScheme {
	protected final String baseName;
	@Nullable protected final String configurationNamePrefix;
	@Nullable protected final String componentDisplayName;
	protected final List<String> dimensions;

	private NamingScheme(String baseName, @Nullable String configurationNamePrefix, @Nullable String componentDisplayName) {
		this(baseName, configurationNamePrefix, componentDisplayName, emptyList());
	}

	private NamingScheme(String baseName, @Nullable String configurationNamePrefix, @Nullable String componentDisplayName, List<String> dimensions) {
		this.baseName = baseName;
		this.configurationNamePrefix = configurationNamePrefix;
		this.componentDisplayName = componentDisplayName;
		this.dimensions = dimensions;
	}

	public static NamingScheme asMainComponent(String baseName) {
		return new Main(baseName);
	}

	public abstract String getConfigurationName(String target);

	public abstract String getConfigurationNameWithoutPrefix(String target);

	public BaseNameNamingScheme getBaseName() {
		return new BaseNameNamingScheme();
	}

	public String getResourcePath(GroupId groupId) {
		return groupId.get().map(it -> it.replace('.', '/') + '/').orElse("") + String.join("-", dimensions);
	}

	/**
	 * Creates a copy of this scheme, <em>adding</em> a variant dimension if required.
	 */
	public <T extends Named> NamingScheme withVariantDimension(T value, Collection<? extends T> allValuesForAxis) {
		if (allValuesForAxis.size() == 1) {
			return this;
		}

		List<String> newDimensions = new ArrayList<>();
		newDimensions.addAll(dimensions);
		newDimensions.add(value.getName());
		return new Other(baseName, newDimensions);
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

	public abstract String getTaskName(String verb);

	public String getTaskName(String verb, String object) {
		return getTaskName(verb) + StringUtils.capitalize(object);
	}

	public abstract String getOutputDirectoryBase(String outputType);

    public abstract NamingScheme withConfigurationNamePrefix(String configurationNamePrefix);

	public abstract NamingScheme withComponentDisplayName(String componentDisplayName);

	public String getConfigurationDescription(String format) {
		Preconditions.checkState(componentDisplayName != null, "make sure component display name is set");
		return String.format(format, componentDisplayName);
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
			values.addAll(dimensions);
			return String.join("-", values);
		}
	}

	private static class Main extends NamingScheme {
		public Main(String baseName) {
			this(baseName, null, null);
		}

		public Main(String baseName, @Nullable String configurationNamePrefix, @Nullable String componentDisplayName) {
			super(baseName, configurationNamePrefix, componentDisplayName);
		}

		@Override
		public String getConfigurationName(String target) {
			if (configurationNamePrefix == null) {
				return target;
			}
			return configurationNamePrefix + StringUtils.capitalize(target);
		}

		@Override
		public String getConfigurationNameWithoutPrefix(String target) {
			return target;
		}

		@Override
		public String getTaskName(String verb) {
			return verb;
		}

		@Override
		public String getOutputDirectoryBase(String outputType) {
			return outputType + "/main";
		}

		@Override
		public NamingScheme withConfigurationNamePrefix(String configurationNamePrefix) {
			return new Main(baseName, configurationNamePrefix, componentDisplayName);
		}

		@Override
		public NamingScheme withComponentDisplayName(String componentDisplayName) {
			return new Main(baseName, configurationNamePrefix, componentDisplayName);
		}
	}

	private static class Other extends NamingScheme {
		private final String dimensionPrefix;

		Other(String baseName, List<String> dimensions) {
			this(baseName, null, null, dimensions);
		}

		Other(String baseName, @Nullable String configurationNamePrefix, @Nullable String componentDisplayName, List<String> dimensions) {
			super(baseName, configurationNamePrefix, componentDisplayName, dimensions);
			this.dimensionPrefix = createPrefix(dimensions);
		}

		@Override
		public String getConfigurationName(String target) {
			if (configurationNamePrefix == null) {
				return dimensionPrefix + StringUtils.capitalize(target);
			}
			return dimensionPrefix + StringUtils.capitalize(configurationNamePrefix) + StringUtils.capitalize(target);
		}

		@Override
		public String getConfigurationNameWithoutPrefix(String target) {
			return dimensionPrefix + StringUtils.capitalize(target);
		}

		@Override
		public String getTaskName(String verb) {
			return verb + StringUtils.capitalize(dimensionPrefix);
		}

		private String createPrefix(List<String> dimensions) {
			if (dimensions.isEmpty()) {
				return "";
			}
			return StringUtils.uncapitalize(dimensions.stream().map(StringUtils::capitalize).collect(Collectors.joining()));
		}

		@Override
		public String getOutputDirectoryBase(String outputType) {
			return outputType + "/main/" + dimensionPrefix;
		}

		@Override
		public NamingScheme withConfigurationNamePrefix(String configurationNamePrefix) {
			return new Other(baseName, configurationNamePrefix, componentDisplayName, dimensions);
		}

		@Override
		public NamingScheme withComponentDisplayName(String componentDisplayName) {
			return new Other(baseName, configurationNamePrefix, componentDisplayName, dimensions);
		}
	}

}
