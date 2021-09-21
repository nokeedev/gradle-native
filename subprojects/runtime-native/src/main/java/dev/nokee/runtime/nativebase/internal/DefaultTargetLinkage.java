/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateAxis;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.utils.ConfigurationUtils;
import org.gradle.api.attributes.AttributeContainer;

import static dev.nokee.runtime.nativebase.BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS;

final class DefaultTargetLinkage implements TargetLinkage, Coordinate<BinaryLinkage>, ConfigurationUtils.ConfigurationAttributesProvider {
	private final BinaryLinkage binaryLinkage;

	DefaultTargetLinkage(BinaryLinkage binaryLinkage) {
		this.binaryLinkage = binaryLinkage;
	}

	@Override
	public void forConsuming(AttributeContainer attributes) {
		// no attributes required, use selection rule instead
	}

	@Override
	public void forResolving(AttributeContainer attributes) {
		attributes.attribute(BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE, binaryLinkage);
	}

	@Override
	public BinaryLinkage getValue() {
		return binaryLinkage;
	}

	@Override
	public CoordinateAxis<BinaryLinkage> getAxis() {
		return BINARY_LINKAGE_COORDINATE_AXIS;
	}

	@Override
	public String toString() {
		return binaryLinkage.getName();
	}
}
