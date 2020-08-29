package dev.nokee.platform.base.internal;

import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.runtime.base.internal.Dimension;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Named;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
public class VariantIdentifier implements DomainObjectIdentifierInternal {
	String unambiguousName;
	ComponentIdentifier componentIdentifier;
	@Getter(AccessLevel.NONE) List<String> dimensions;

	public VariantIdentifier(String unambiguousName, ComponentIdentifier componentIdentifier) {
		this(unambiguousName, componentIdentifier, Collections.emptyList());
	}

	public static VariantIdentifier of(String unambiguousName, ComponentIdentifier identifier) {
		return new VariantIdentifier(unambiguousName, identifier, Collections.emptyList());
	}

	@Override
	public Optional<ComponentIdentifier> getParentIdentifier() {
		return Optional.of(componentIdentifier);
	}

	@Override
	// TODO: The display name should be richer when we wire the BuildVariant concept in the identifier
	public String getDisplayName() {
		val builder = new StringBuilder();
		if (!unambiguousName.isEmpty()) {
			builder.append("variant").append(" '").append(unambiguousName).append("'");
		}
		if (unambiguousName.isEmpty()) {
			builder.append(componentIdentifier.getDisplayName());
		} else if (!componentIdentifier.isMainComponent() || componentIdentifier.hasCustomDisplayName()) {
			builder.append(" of ").append(componentIdentifier.getDisplayName());
		}
		return builder.toString();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private final List<String> allDimensions = new ArrayList<>();
		private final List<String> dimensions = new ArrayList<>();
		private ComponentIdentifier componentIdentifier = null;

		public <T extends Named> Builder withVariantDimension(T value, Collection<? extends T> allValuesForAxis) {
			allDimensions.add(value.getName());
			if (allValuesForAxis.size() == 1) {
				return this;
			}

			dimensions.add(value.getName());
			return this;
		}

		public Builder withUnambiguousNameFromBuildVariants(BuildVariantInternal value, Collection<? extends BuildVariantInternal> allBuildVariants) {
			int index = 0;
			for (Dimension dimension : value.getDimensions()) {
				if (dimension instanceof Named) {
					Set<Named> allValuesForAxis = allBuildVariants.stream().map(extractDimensionAtIndex(index)).collect(Collectors.toSet());
					withVariantDimension((Named)dimension, allValuesForAxis);
				} else {
					throw new IllegalArgumentException("The dimension needs to implement Named, it's an implementation detail at this point");
				}
				index++;
			}

			return this;
		}

		private Function<BuildVariantInternal, Named> extractDimensionAtIndex(int index) {
			return buildVariant -> (Named)buildVariant.getDimensions().get(index);
		}

		public Builder withComponentIdentifier(ComponentIdentifier componentIdentifier) {
			this.componentIdentifier = componentIdentifier;
			return this;
		}

		public VariantIdentifier build() {
			var allDimensions = this.allDimensions;
			if (allDimensions.size() == dimensions.size()) {
				allDimensions = Collections.emptyList();
			}
			return new VariantIdentifier(createUnambiguousName(dimensions), componentIdentifier, allDimensions);
		}

		private static String createUnambiguousName(List<String> dimensions) {
			return StringUtils.uncapitalize(dimensions.stream().map(StringUtils::capitalize).collect(Collectors.joining()));
		}
	}
}
