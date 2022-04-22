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
package dev.nokee.platform.jni.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import dev.nokee.language.base.ConfigurableSourceSet;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.nativebase.HasHeaders;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.model.internal.DomainObjectIdentifierUtils;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.actions.ModelAction;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.IsVariant;
import dev.nokee.platform.base.internal.VariantIdentifier;
import lombok.val;
import org.gradle.api.Project;

import static dev.nokee.runtime.nativebase.TargetMachine.TARGET_MACHINE_COORDINATE_AXIS;

public final class JavaNativeInterfaceLibraryVariantRegistrationFactory {
	private final Project project;

	public JavaNativeInterfaceLibraryVariantRegistrationFactory(Project project) {
		this.project = project;
	}

	@SuppressWarnings("unchecked")
	public ModelRegistration create(VariantIdentifier<?> identifier) {
		val buildVariant = (BuildVariantInternal) identifier.getBuildVariant();
		Preconditions.checkArgument(buildVariant.hasAxisValue(TARGET_MACHINE_COORDINATE_AXIS));

		return ModelRegistration.builder()
			.withComponent(IsVariant.tag())
			.withComponent(ConfigurableTag.tag())
			.withComponent(new IdentifierComponent(identifier))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(IsLanguageSourceSet.class), ModelComponentReference.ofProjection(LanguageSourceSet.class), ModelComponentReference.of(ModelState.IsAtLeastRegistered.class), (entity, id, tag, knownSourceSet, ignored) -> {
				if (DomainObjectIdentifierUtils.isDescendent(id.get(), identifier) && HasHeaders.class.isAssignableFrom(knownSourceSet.getType().getConcreteType())) {
					project.getExtensions().getByType(ModelRegistry.class).instantiate(ModelAction.configure(entity.getId(), LanguageSourceSet.class, sourceSet -> {
						((ConfigurableSourceSet) ((HasHeaders) sourceSet).getHeaders()).convention("src/" + identifier.getComponentIdentifier().getName() + "/headers");
					}));
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(IsLanguageSourceSet.class), ModelComponentReference.ofProjection(LanguageSourceSet.class), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (entity, id, tag, knownSourceSet, ignored) -> {
				if (DomainObjectIdentifierUtils.isDescendent(id.get(), identifier)) {
					project.getExtensions().getByType(ModelRegistry.class).instantiate(ModelAction.configure(entity.getId(), LanguageSourceSet.class, sourceSet -> {
						if (sourceSet instanceof ObjectiveCSourceSet) {
							sourceSet.convention(ImmutableList.of("src/" + identifier.getComponentIdentifier().getName() + "/" + ((LanguageSourceSetIdentifier) id.get()).getName().get(), "src/" + identifier.getComponentIdentifier().getName() + "/objc"));
						} else if (sourceSet instanceof ObjectiveCppSourceSet) {
							sourceSet.convention(ImmutableList.of("src/" + identifier.getComponentIdentifier().getName() + "/" + ((LanguageSourceSetIdentifier) id.get()).getName().get(), "src/" + identifier.getComponentIdentifier().getName() + "/objcpp"));
						} else {
							sourceSet.convention("src/" + identifier.getComponentIdentifier().getName() + "/" + ((LanguageSourceSetIdentifier) id.get()).getName().get());
						}
					}));
				}
			}))
			.build();
	}
}
