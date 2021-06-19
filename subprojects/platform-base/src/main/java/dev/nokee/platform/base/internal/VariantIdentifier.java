package dev.nokee.platform.base.internal;

import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.model.internal.NamedDomainObjectIdentifier;
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Variant;
import dev.nokee.runtime.core.Coordinate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;
import lombok.var;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Named;
import org.gradle.util.Path;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

	public VariantIdentifier(Class<T> type, ComponentIdentifier<?> componentIdentifier, DefaultBuildVariant buildVariant) {
		this(buildVariant.getName(), type, componentIdentifier, buildVariant.getAmbiguousDimensions(), buildVariant.getAllDimensions(), buildVariant, buildVariant.getAllDimensions().getAsLowerCamelCase().get());
	}

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
		return StringUtils.uncapitalize(((BuildVariantInternal)buildVariant).getDimensions().stream().map(Coordinate::getValue).map(Named.class::cast).map(Named::getName).map(StringUtils::capitalize).collect(Collectors.joining()));
	}

	private static List<String> createAmbiguousDimensionNames(BuildVariant buildVariant) {
		return ((BuildVariantInternal)buildVariant).getDimensions().stream().map(Coordinate::getValue).map(Named.class::cast).map(Named::getName).collect(Collectors.toList());
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
	public Path getPath() {
		return getComponentIdentifier().getPath().child(fullName);
	}

	@Override
	public String toString() {
		return "variant '" + getPath() + "' (" + type.getSimpleName() + ")";
	}

	public static Builder<Variant> builder() {
		return new Builder<>();
	}

	public static class Builder<T extends Variant> {
		private Dimensions allDimensions = Dimensions.empty();
		private Dimensions dimensions = Dimensions.empty();
		private ComponentIdentifier<?> componentIdentifier = null;
		private BuildVariantInternal buildVariant = null;
		private Class<? extends T> type;

		@SuppressWarnings("unchecked")
		public <S extends T> Builder<S> withType(Class<S> type) {
			this.type = type;
			return (Builder<S>) this;
		}

		@Deprecated // used in tests
		public <V extends Named> Builder<T> withVariantDimension(V value, Collection<? extends V> allValuesForAxis) {
			return withVariantDimension(value.getName(), allValuesForAxis);
		}

		@Deprecated // used in tests
		public Builder<T> withVariantDimension(String name, Collection<?> allValuesForAxis) {
			allDimensions = allDimensions.add(name);
			if (allValuesForAxis.size() == 1) {
				return this;
			}

			dimensions = dimensions.add(name);
			return this;
		}

		public Builder<T> withBuildVariant(BuildVariantInternal buildVariant) {
			this.buildVariant = buildVariant;
			return this;
		}

		public Builder<T> withComponentIdentifier(ComponentIdentifier<?> componentIdentifier) {
			this.componentIdentifier = componentIdentifier;
			return this;
		}

		public VariantIdentifier<T> build() {
			@SuppressWarnings("unchecked")
			val variantType = (Class<T>) type;
			if (buildVariant instanceof DefaultBuildVariant) {
				return new VariantIdentifier<T>(variantType, componentIdentifier, (DefaultBuildVariant) buildVariant);
			}
			var allDimensions = this.allDimensions;
			if (allDimensions.size() == dimensions.size()) {
				allDimensions = Dimensions.empty();
			}
			return new VariantIdentifier<T>(dimensions.getAsLowerCamelCase().orElse(""), variantType, componentIdentifier, dimensions, allDimensions, buildVariant, this.allDimensions.getAsLowerCamelCase().orElse(""));
		}
	}
}
