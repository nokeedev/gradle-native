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

import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.discover.DisRule;
import dev.nokee.model.internal.discover.Discovery;
import dev.nokee.model.internal.discover.RealizeRule;
import dev.nokee.model.internal.discover.SelectRule;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import org.gradle.api.Task;
import org.gradle.api.specs.Spec;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public /*final*/ class BinaryLifecycleTaskDiscovery implements Discovery {
	private static Spec<ModelObjectIdentifier> linkageOf(Spec<? super BinaryLinkage> spec) {
		return identifier -> {
			if (identifier instanceof VariantIdentifier) {
				return ((BuildVariantInternal) ((VariantIdentifier) identifier).getBuildVariant()).findAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS).map(spec::isSatisfiedBy).filter(it -> it).orElse(false);
			} else {
				return false;
			}
		};
	}
	private static final Spec<ModelObjectIdentifier> EXECUTABLE_LINKAGE = linkageOf(BinaryLinkage::isExecutable);
	private static final Spec<ModelObjectIdentifier> SHARED_LINKAGE = linkageOf(BinaryLinkage::isShared);
	private static final Spec<ModelObjectIdentifier> STATIC_LINKAGE = linkageOf(BinaryLinkage::isStatic);
	private static final Spec<ModelObjectIdentifier> BUNDLE_LINKAGE = linkageOf(BinaryLinkage::isBundle);

	@Override
	public <T> List<DisRule> discover(ModelType<T> discoveringType) {
		final DisRule rule = new SelectRule(discoveringType, Arrays.asList(
			new SelectRule.Case(EXECUTABLE_LINKAGE, TaskName.of("executable"), ModelType.of(Task.class)),
			new SelectRule.Case(SHARED_LINKAGE, TaskName.of("sharedLibrary"), ModelType.of(Task.class)),
			new SelectRule.Case(STATIC_LINKAGE, TaskName.of("staticLibrary"), ModelType.of(Task.class)),
			new SelectRule.Case(BUNDLE_LINKAGE, TaskName.of("bundle"), ModelType.of(Task.class))
		));

		return Collections.singletonList(new RealizeRule(rule));
	}
}