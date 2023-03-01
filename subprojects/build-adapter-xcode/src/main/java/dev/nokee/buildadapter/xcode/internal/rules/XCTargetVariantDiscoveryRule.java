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
package dev.nokee.buildadapter.xcode.internal.rules;

import com.google.common.collect.ImmutableSet;
import dev.nokee.buildadapter.xcode.internal.XcodeConfigurationParameter;
import dev.nokee.buildadapter.xcode.internal.components.XCTargetComponent;
import dev.nokee.model.capabilities.variants.KnownVariantInformationElement;
import dev.nokee.model.internal.buffers.ModelBuffers;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.tags.ModelComponentTag;
import dev.nokee.platform.base.internal.IsComponent;
import dev.nokee.xcode.XCLoader;
import dev.nokee.xcode.XCTargetReference;
import lombok.val;

public final class XCTargetVariantDiscoveryRule extends ModelActionWithInputs.ModelAction2<ModelComponentTag<IsComponent>, XCTargetComponent> {
	private final XCLoader<Iterable<String>, XCTargetReference> loader;
	private final XcodeConfigurationParameter configurationParameter;

	public XCTargetVariantDiscoveryRule(XCLoader<Iterable<String>, XCTargetReference> loader, XcodeConfigurationParameter configurationParameter) {
		this.loader = loader;
		this.configurationParameter = configurationParameter;
	}

	@Override
	protected void execute(ModelNode entity, ModelComponentTag<IsComponent> ignored1, XCTargetComponent target) {
		val builder = ImmutableSet.<KnownVariantInformationElement>builder();
		val requestedConfiguration = configurationParameter.get();
		target.get().load(loader).forEach(configuration -> {
			if (requestedConfiguration == null || configuration.equals(requestedConfiguration)) {
				builder.add(new KnownVariantInformationElement(configuration));
			}
		});
		entity.addComponent(ModelBuffers.of(KnownVariantInformationElement.class, builder.build()));
	}
}
