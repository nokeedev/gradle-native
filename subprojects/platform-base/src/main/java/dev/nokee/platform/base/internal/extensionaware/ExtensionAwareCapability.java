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
package dev.nokee.platform.base.internal.extensionaware;

import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.tags.ModelTags;
import org.gradle.api.Plugin;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;

import javax.inject.Inject;

public class ExtensionAwareCapability<T extends ExtensionAware & PluginAware> implements Plugin<T> {
	private ObjectFactory objects;

	@Inject
	public ExtensionAwareCapability(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(T target) {
		target.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(ExtensionAwareMixIn.Tag.class), (entity, ignored1) -> {
			entity.addComponent(new ExtensionAwareComponent(objects.newInstance(ExtensionContainerProvider.class).getExtensions()));
		}));
	}

	public interface ExtensionContainerProvider extends ExtensionAware {}
}
