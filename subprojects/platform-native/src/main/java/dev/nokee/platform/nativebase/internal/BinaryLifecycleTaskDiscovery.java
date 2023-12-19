/*
 * Copyright 2023 the original author or authors.
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

package dev.nokee.platform.nativebase.internal;

import dev.nokee.model.internal.DiscoveredElements;
import dev.nokee.model.internal.ModelElementDiscovery;
import dev.nokee.model.internal.ModelObjectIdentity;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import org.gradle.api.Task;

import java.util.Collections;
import java.util.List;

public /*final*/ class BinaryLifecycleTaskDiscovery implements ModelElementDiscovery {
	@Override
	public List<DiscoveredElements.DiscoverableElement> discover(ModelObjectIdentity<?> identity) {
		assert identity.getIdentifier() instanceof VariantIdentifier;
		final BuildVariantInternal buildVariant = (BuildVariantInternal) ((VariantIdentifier) identity.getIdentifier()).getBuildVariant();
		final BinaryLinkage linkage = buildVariant.getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS);
		if (linkage.isExecutable()) {
			return Collections.singletonList(new DiscoveredElements.DiscoverableElement(id -> id.child(TaskName.of("executable")), ModelType.of(Task.class)));
		} else if (linkage.isShared()) {
			return Collections.singletonList(new DiscoveredElements.DiscoverableElement(id -> id.child(TaskName.of("sharedLibrary")), ModelType.of(Task.class)));
		} else if (linkage.isStatic()) {
			return Collections.singletonList(new DiscoveredElements.DiscoverableElement(id -> id.child(TaskName.of("staticLibrary")), ModelType.of(Task.class)));
		}
		throw new UnsupportedOperationException();
	}
}
