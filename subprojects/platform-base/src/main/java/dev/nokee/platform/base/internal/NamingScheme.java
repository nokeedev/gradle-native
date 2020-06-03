package dev.nokee.platform.base.internal;

import dev.nokee.runtime.base.internal.Dimension;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Named;
import org.gradle.util.GUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public abstract class NamingScheme {
	private final String baseName;
	private final List<String> dimensions;

	private NamingScheme(String baseName) {
		this(baseName, emptyList());
	}

	private NamingScheme(String baseName, List<String> dimensions) {
		this.baseName = baseName;
		this.dimensions = dimensions;
	}

	public static NamingScheme asMainComponent(String baseName) {
		return new Main(baseName);
	}

	public abstract String getConfigurationName(String target);

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

		List<String> newDimmensions = new ArrayList<>();
		newDimmensions.addAll(dimensions);
		newDimmensions.add(value.getName());
		return new Other(baseName, newDimmensions);
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
			super(baseName);
		}

		public String getConfigurationName(String target) {
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
	}

	private static class Other extends NamingScheme {
		private final String dimensionPrefix;

		Other(String baseName, List<String> dimensions) {
			super(baseName, dimensions);
			this.dimensionPrefix = createPrefix(dimensions);
		}

		public String getConfigurationName(String target) {
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
	}

}
