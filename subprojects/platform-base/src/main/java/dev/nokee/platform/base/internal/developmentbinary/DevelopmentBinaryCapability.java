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
package dev.nokee.platform.base.internal.developmentbinary;

import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelComponentTag;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;
import org.gradle.api.provider.Property;

import static dev.nokee.model.internal.core.ModelPropertyRegistrationFactory.property;
import static dev.nokee.model.internal.state.ModelStates.register;
import static dev.nokee.utils.ProviderUtils.finalizeValue;

public class DevelopmentBinaryCapability<T extends ExtensionAware & PluginAware> implements Plugin<T> {
	@Override
	public void apply(T target) {
		target.getExtensions().getByType(ModelConfigurer.class)
			.configure(new OnDiscover(new RegisterDevelopmentBinaryPropertyRule(target.getExtensions().getByType(ModelRegistry.class))));
		target.getExtensions().getByType(ModelConfigurer.class)
			.configure(new FinalizeDevelopmentBinaryPropertyRule());
	}

	private static final class RegisterDevelopmentBinaryPropertyRule extends ModelActionWithInputs.ModelAction1<ModelComponentTag<HasDevelopmentBinaryMixIn.Tag>> {
		private final ModelRegistry registry;

		private RegisterDevelopmentBinaryPropertyRule(ModelRegistry registry) {
			this.registry = registry;
		}

		@Override
		protected void execute(ModelNode entity, ModelComponentTag<HasDevelopmentBinaryMixIn.Tag> ignored) {
			val developmentBinaryProperty = register(registry.instantiate(ModelRegistration.builder()
				.withComponent(new ElementNameComponent("developmentBinary"))
				.withComponent(new ParentComponent(entity))
				.mergeFrom(property(Binary.class))
				.build()));
			entity.addComponent(new DevelopmentBinaryPropertyComponent(developmentBinaryProperty));
		}
	}

	private static final class FinalizeDevelopmentBinaryPropertyRule extends ModelActionWithInputs.ModelAction2<DevelopmentBinaryPropertyComponent, ModelStates.Finalizing> {
		@Override
		protected void execute(ModelNode entity, DevelopmentBinaryPropertyComponent developmentBinary, ModelStates.Finalizing ignored1) {
			@SuppressWarnings("unchecked")
			val property = (Property<Binary>) developmentBinary.get().get(GradlePropertyComponent.class).get();
			entity.addComponent(new DevelopmentBinaryComponent(finalizeValue(property).getOrNull()));
		}
	}
}
