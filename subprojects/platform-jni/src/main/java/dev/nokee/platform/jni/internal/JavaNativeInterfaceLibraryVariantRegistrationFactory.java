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
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.IsVariant;
import dev.nokee.platform.base.internal.VariantIdentifier;
import lombok.val;
import org.gradle.api.Project;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.DomainObjectEntities.tagsOf;
import static dev.nokee.runtime.nativebase.TargetMachine.TARGET_MACHINE_COORDINATE_AXIS;

public final class JavaNativeInterfaceLibraryVariantRegistrationFactory {
	private final Project project;

	public JavaNativeInterfaceLibraryVariantRegistrationFactory(Project project) {
		this.project = project;
	}

	public ModelRegistration create(VariantIdentifier identifier) {
		val buildVariant = (BuildVariantInternal) identifier.getBuildVariant();
		Preconditions.checkArgument(buildVariant.hasAxisValue(TARGET_MACHINE_COORDINATE_AXIS));

		return ModelRegistration.builder()
			.withComponent(tag(IsVariant.class))
			.withComponent(tag(ConfigurableTag.class))
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(tag(JniLibraryVariantTag.class))
			.mergeFrom(tagsOf(JniLibraryInternal.class))
			.withComponent(createdUsing(of(JniLibraryInternal.class), () -> project.getObjects().newInstance(JniLibraryInternal.class, identifier, project.getObjects())))
			.build();
	}
}
