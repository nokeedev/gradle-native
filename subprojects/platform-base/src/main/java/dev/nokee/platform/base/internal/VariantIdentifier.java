package dev.nokee.platform.base.internal;

import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Variant;
import dev.nokee.runtime.base.internal.Dimension;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Named;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@ToString
@EqualsAndHashCode
public class VariantIdentifier<T extends Variant> implements DomainObjectIdentifierInternal {
	@Getter private final String unambiguousName;
	@Getter private final Class<T> type;
	@Getter private final ComponentIdentifier<?> componentIdentifier;
	private final List<String> dimensions;
	@EqualsAndHashCode.Exclude private final BuildVariant buildVariant;

	public VariantIdentifier(String unambiguousName, Class<T> type, ComponentIdentifier<?> componentIdentifier, List<String> dimensions, BuildVariant buildVariant) {
		this.unambiguousName = requireNonNull(unambiguousName);
		this.type = requireNonNull(type);
		this.componentIdentifier = requireNonNull(componentIdentifier);
		this.dimensions = requireNonNull(dimensions);
		this.buildVariant = buildVariant;
	}

	public static <T extends Variant> VariantIdentifier<T> of(String unambiguousName, Class<T> type, ComponentIdentifier<?> identifier) {
		return new VariantIdentifier<>(unambiguousName, type, identifier, Collections.emptyList(), null);
	}

	public BuildVariant getBuildVariant() {
		return requireNonNull(buildVariant);
	}

	@Override
	public Optional<? extends ComponentIdentifier<?>> getParentIdentifier() {
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

	public static Builder<Variant> builder() {
		return new Builder<>();
	}

	public static class Builder<T extends Variant> {
		private final List<String> allDimensions = new ArrayList<>();
		private final List<String> dimensions = new ArrayList<>();
		private ComponentIdentifier<?> componentIdentifier = null;
		private BuildVariant buildVariant = null;
		private Class<? extends T> type;

		@SuppressWarnings("unchecked")
		public <S extends T> Builder<S> withType(Class<S> type) {
			this.type = type;
			return (Builder<S>) this;
		}

		public <V extends Named> Builder<T> withVariantDimension(V value, Collection<? extends V> allValuesForAxis) {
			allDimensions.add(value.getName());
			if (allValuesForAxis.size() == 1) {
				return this;
			}

			dimensions.add(value.getName());
			return this;
		}

		@SuppressWarnings("unchecked")
		public Builder<T> withUnambiguousNameFromBuildVariants(BuildVariant value, Collection<? extends BuildVariant> allBuildVariants) {
			return withUnambiguousNameFromBuildVariants((BuildVariantInternal) value, (Collection<? extends BuildVariantInternal>) allBuildVariants);
		}

		public Builder<T> withUnambiguousNameFromBuildVariants(BuildVariantInternal value, Collection<? extends BuildVariantInternal> allBuildVariants) {
			buildVariant = value;
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

		public Builder<T> withUnambiguousNameFromBuildVariant(BuildVariant buildVariant) {
			val dimensions = ((BuildVariantInternal) buildVariant).getDimensions().stream().map(Named.class::cast).map(Named::getName).collect(Collectors.toList());
			this.dimensions.addAll(dimensions);
			this.allDimensions.addAll(dimensions);
			this.buildVariant = buildVariant;
			return this;
		}

		private Function<BuildVariantInternal, Named> extractDimensionAtIndex(int index) {
			return buildVariant -> (Named)buildVariant.getDimensions().get(index);
		}

		public Builder<T> withComponentIdentifier(ComponentIdentifier<?> componentIdentifier) {
			this.componentIdentifier = componentIdentifier;
			return this;
		}

		public VariantIdentifier<T> build() {
			var allDimensions = this.allDimensions;
			if (allDimensions.size() == dimensions.size()) {
				allDimensions = Collections.emptyList();
			}
			@SuppressWarnings("unchecked")
			val variantType = (Class<T>) type;
			return new VariantIdentifier<>(createUnambiguousName(dimensions), variantType, componentIdentifier, allDimensions, buildVariant);
		}

		private static String createUnambiguousName(List<String> dimensions) {
			return StringUtils.uncapitalize(dimensions.stream().map(StringUtils::capitalize).collect(Collectors.joining()));
		}
	}
}
