package dev.nokee.platform.base.internal;

import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.model.internal.NamedDomainObjectIdentifier;
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier;
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

@EqualsAndHashCode
public final class VariantIdentifier<T extends Variant> implements DomainObjectIdentifierInternal, NamedDomainObjectIdentifier, TypeAwareDomainObjectIdentifier<T> {
	@Getter private final String unambiguousName;
	@Getter private final Class<T> type;
	@Getter private final ComponentIdentifier<?> componentIdentifier;
	@Getter @EqualsAndHashCode.Exclude private final Dimensions ambiguousDimensions;
	private final Dimensions dimensions;
	@EqualsAndHashCode.Exclude private final BuildVariant buildVariant;
	@EqualsAndHashCode.Exclude private final String fullName;

	public VariantIdentifier(String unambiguousName, Class<T> type, ComponentIdentifier<?> componentIdentifier, Dimensions ambiguousDimensions, Dimensions dimensions, BuildVariant buildVariant, String fullName) {
		this.unambiguousName = requireNonNull(unambiguousName);
		this.type = requireNonNull(type);
		this.componentIdentifier = requireNonNull(componentIdentifier);
		this.ambiguousDimensions = ambiguousDimensions;
		this.dimensions = requireNonNull(dimensions);
		this.buildVariant = buildVariant;
		this.fullName = fullName;
	}

	public static <T extends Variant> VariantIdentifier<T> of(String unambiguousName, Class<T> type, ComponentIdentifier<?> identifier) {
		return new VariantIdentifier<>(unambiguousName, type, identifier, Dimensions.empty(), Dimensions.empty(), null, unambiguousName);
	}

	public static <T extends Variant> VariantIdentifier<T> of(BuildVariant buildVariant, Class<T> type, ComponentIdentifier<?> identifier) {
		String unambiguousName = createUnambiguousName(buildVariant);
		Dimensions ambiguousDimensions = Dimensions.of(createAmbiguousDimensionNames(buildVariant));
		return new VariantIdentifier<>(unambiguousName, type, identifier, ambiguousDimensions, Dimensions.empty(), buildVariant, unambiguousName);
	}

	private static String createUnambiguousName(BuildVariant buildVariant) {
		return StringUtils.uncapitalize(((BuildVariantInternal)buildVariant).getDimensions().stream().map(Named.class::cast).map(Named::getName).map(StringUtils::capitalize).collect(Collectors.joining()));
	}

	private static List<String> createAmbiguousDimensionNames(BuildVariant buildVariant) {
		return ((BuildVariantInternal)buildVariant).getDimensions().stream().map(Named.class::cast).map(Named::getName).collect(Collectors.toList());
	}

	public String getName() {
		return unambiguousName;
	}

	public String getFullName() {
		return fullName;
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

	@Override
	public String toString() {
		return "variant " + type.getSimpleName() + " '" + componentIdentifier.getProjectIdentifier().getPath().toString() + ":" + componentIdentifier.getName().get() + ":" + getFullName() + "'";
	}

	public static Builder<Variant> builder() {
		return new Builder<>();
	}

	public static class Builder<T extends Variant> {
		private Dimensions allDimensions = Dimensions.empty();
		private Dimensions dimensions = Dimensions.empty();
		private ComponentIdentifier<?> componentIdentifier = null;
		private BuildVariant buildVariant = null;
		private Class<? extends T> type;

		@SuppressWarnings("unchecked")
		public <S extends T> Builder<S> withType(Class<S> type) {
			this.type = type;
			return (Builder<S>) this;
		}

		public <V extends Named> Builder<T> withVariantDimension(V value, Collection<? extends V> allValuesForAxis) {
			allDimensions = allDimensions.add(value.getName());
			if (allValuesForAxis.size() == 1) {
				return this;
			}

			dimensions = dimensions.add(value.getName());
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
				allDimensions = Dimensions.empty();
			}
			@SuppressWarnings("unchecked")
			val variantType = (Class<T>) type;
			return new VariantIdentifier<T>(dimensions.getAsLowerCamelCase().orElse(""), variantType, componentIdentifier, dimensions, allDimensions, buildVariant, this.allDimensions.getAsLowerCamelCase().orElse(""));
		}
	}
}
