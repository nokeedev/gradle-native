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
package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.model.internal.KnownModelObject;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import java.util.Set;
import java.util.stream.StreamSupport;

import static dev.nokee.platform.nativebase.internal.NativeVariantComparators.preferDebugBuildType;
import static dev.nokee.platform.nativebase.internal.NativeVariantComparators.preferHostMachineArchitecture;
import static dev.nokee.platform.nativebase.internal.NativeVariantComparators.preferHostOperatingSystemFamily;
import static dev.nokee.platform.nativebase.internal.NativeVariantComparators.preferSharedBinaryLinkage;

public class BuildableDevelopmentVariantTransformer<T extends VariantInternal> implements Transformer<Provider<T>, Set<KnownModelObject<VariantInternal>>> {
	@Override
	@SuppressWarnings("unchecked")
	public Provider<T> transform(Set<KnownModelObject<VariantInternal>> it) {
		final KnownModelObject<VariantInternal> result = StreamSupport.stream(it.spliterator(), false).min(preferHostOperatingSystemFamily().thenComparing(preferHostMachineArchitecture()).thenComparing(preferSharedBinaryLinkage()).thenComparing(preferDebugBuildType())).orElseThrow(() -> new RuntimeException("No variants available."));
		if (isBuildable(result)) {
			return (Provider<T>) result.asProvider();
		}
		return null;
	}

	public boolean isBuildable(KnownModelObject<VariantInternal> variant) {
		return ((VariantIdentifier) variant.getIdentifier()).getBuildVariant().hasAxisOf(Coordinate.of(OperatingSystemFamily.OPERATING_SYSTEM_COORDINATE_AXIS, OperatingSystemFamily.forName(System.getProperty("os.name"))));
	}
}
