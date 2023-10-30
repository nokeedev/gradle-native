/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.HasName;
import dev.nokee.model.internal.DefaultModelObjectIdentifier;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Variant;
import dev.nokee.runtime.core.Coordinate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Named;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toGradlePath;
import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
public final class VariantIdentifier implements ModelObjectIdentifier, HasName {
	@Getter private final String unambiguousName;
	@Getter private final DomainObjectIdentifier ownerIdentifier;
	@Getter @EqualsAndHashCode.Exclude private final Dimensions ambiguousDimensions;
	private final Dimensions dimensions;
	@EqualsAndHashCode.Exclude private final BuildVariant buildVariant;
	@EqualsAndHashCode.Exclude private final String fullName;

	public VariantIdentifier(DomainObjectIdentifier ownerIdentifier, DefaultBuildVariant buildVariant) {
		this(buildVariant.getName(), ownerIdentifier, buildVariant.getAmbiguousDimensions(), buildVariant.getAllDimensions(), buildVariant, buildVariant.getAllDimensions().getAsLowerCamelCase().get());
	}

	public VariantIdentifier(String unambiguousName, DomainObjectIdentifier ownerIdentifier, Dimensions ambiguousDimensions, Dimensions dimensions, BuildVariant buildVariant, String fullName) {
		this.unambiguousName = requireNonNull(unambiguousName);
		this.ownerIdentifier = requireNonNull(ownerIdentifier);
		this.ambiguousDimensions = ambiguousDimensions;
		this.dimensions = requireNonNull(dimensions);
		this.buildVariant = buildVariant;
		this.fullName = fullName;
	}

	public static VariantIdentifier of(String unambiguousName, DomainObjectIdentifier identifier) {
		return new VariantIdentifier(unambiguousName, identifier, Dimensions.empty(), Dimensions.empty(), null, unambiguousName);
	}

	public static <T extends Variant> VariantIdentifier of(BuildVariant buildVariant, DomainObjectIdentifier identifier) {
		String unambiguousName = createUnambiguousName(buildVariant);
		Dimensions ambiguousDimensions = Dimensions.of(createAmbiguousDimensionNames(buildVariant));
		return new VariantIdentifier(unambiguousName, identifier, ambiguousDimensions, Dimensions.empty(), buildVariant, unambiguousName);
	}

	private static String createUnambiguousName(BuildVariant buildVariant) {
		return StringUtils.uncapitalize(((BuildVariantInternal)buildVariant).getDimensions().stream().map(Coordinate::getValue).map(Named.class::cast).map(Named::getName).map(StringUtils::capitalize).collect(Collectors.joining()));
	}

	private static List<String> createAmbiguousDimensionNames(BuildVariant buildVariant) {
		return ((BuildVariantInternal)buildVariant).getDimensions().stream().map(Coordinate::getValue).map(Named.class::cast).map(Named::getName).collect(Collectors.toList());
	}

	public ElementName getName() {
		return ElementName.of(unambiguousName);
	}

	@Override
	public ModelObjectIdentifier child(ElementName name) {
		return new DefaultModelObjectIdentifier(name, this);
	}

	public String getFullName() {
		return fullName;
	}

	public BuildVariant getBuildVariant() {
		return requireNonNull(buildVariant);
	}

	@Override
	public String toString() {
		if (unambiguousName.isEmpty()) {
			return "default variant '" + toGradlePath(this) + "'";
		}
		return "variant '" + toGradlePath(this) + "'";
	}

	public static Builder<Variant> builder() {
		return new Builder<>();
	}

	@Nullable
	@Override
	public ModelObjectIdentifier getParent() {
		return (ModelObjectIdentifier) ownerIdentifier;
	}

	@Override
	public Iterator<Object> iterator() {
		// TODO: Use identity instead of this
		return ImmutableList.builder().addAll(ownerIdentifier).add(this).build().iterator();
	}

	public static class Builder<T extends Variant> {
		private Dimensions allDimensions = Dimensions.empty();
		private Dimensions dimensions = Dimensions.empty();
		private DomainObjectIdentifier ownerIdentifier = null;
		private BuildVariantInternal buildVariant = null;

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

		public Builder<T> withComponentIdentifier(ModelObjectIdentifier componentIdentifier) {
			this.ownerIdentifier = componentIdentifier;
			return this;
		}

		public VariantIdentifier build() {
			if (buildVariant instanceof DefaultBuildVariant) {
				return new VariantIdentifier(ownerIdentifier, (DefaultBuildVariant) buildVariant);
			}
			Dimensions allDimensions = this.allDimensions;
			if (allDimensions.size() == dimensions.size()) {
				allDimensions = Dimensions.empty();
			}
			return new VariantIdentifier(dimensions.getAsLowerCamelCase().orElse(""), ownerIdentifier, dimensions, allDimensions, buildVariant, this.allDimensions.getAsLowerCamelCase().orElse(""));
		}
	}
}
