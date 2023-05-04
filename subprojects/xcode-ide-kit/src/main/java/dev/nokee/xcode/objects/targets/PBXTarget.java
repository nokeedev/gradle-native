/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.xcode.objects.targets;

import dev.nokee.xcode.objects.PBXProjectItem;
import dev.nokee.xcode.objects.Visitable;
import dev.nokee.xcode.objects.buildphase.PBXBuildPhase;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.files.PBXFileReference;

import java.util.List;
import java.util.Optional;

/**
 * Information for building a specific artifact (a library, binary, or test).
 */
public interface PBXTarget extends PBXProjectItem {
	String getName();

	Optional<ProductType> getProductType();

	List<PBXBuildPhase> getBuildPhases();

	XCConfigurationList getBuildConfigurationList();

	List<PBXTargetDependency> getDependencies();

	Optional<String> getProductName();

	Optional<PBXFileReference> getProductReference();

	@Visitable
	<R> R accept(Visitor<R> visitor);

	Builder toBuilder();

	interface Visitor<R> {
		R visit(PBXAggregateTarget target);

		R visit(PBXLegacyTarget target);

		R visit(PBXNativeTarget target);
	}

	interface Builder {
		Builder productName(String productName);

		PBXTarget build();
	}
}
