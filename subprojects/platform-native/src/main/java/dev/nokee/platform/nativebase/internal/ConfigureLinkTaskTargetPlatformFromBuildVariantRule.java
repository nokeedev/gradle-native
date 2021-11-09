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
package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.nativebase.internal.NativePlatformFactory;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelComponentType;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import dev.nokee.platform.nativebase.tasks.ObjectLink;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.utils.Cast;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.provider.Property;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;

import java.util.function.BiConsumer;

import static dev.nokee.platform.base.internal.util.PropertyUtils.*;

public final class ConfigureLinkTaskTargetPlatformFromBuildVariantRule extends ModelActionWithInputs.ModelAction3<BinaryIdentifier<?>, BuildVariant, NativeLinkTask> {
	private final DomainObjectIdentifier identifier;

	public ConfigureLinkTaskTargetPlatformFromBuildVariantRule(BinaryIdentifier<?> identifier) {
		super(ModelComponentReference.of(Cast.uncheckedCastBecauseOfTypeErasure(BinaryIdentifier.class)), ModelComponentReference.ofAny(ModelComponentType.componentOf(BuildVariant.class)), ModelComponentReference.of(NativeLinkTask.class));
		this.identifier = identifier;
	}

	@Override
	protected void execute(ModelNode entity, BinaryIdentifier<?> identifier, BuildVariant buildVariant, NativeLinkTask linkTask) {
		if (identifier.equals(this.identifier)) {
			linkTask.configure(ObjectLink.class, configureTargetPlatform(set(fromBuildVariant(buildVariant)).andThen(lockProperty())));
		}
	}

	//region Target platform
	private static <SELF extends Task> Action<SELF> configureTargetPlatform(BiConsumer<? super SELF, ? super PropertyUtils.Property<? extends NativePlatform>> action) {
		return task -> {
			action.accept(task, wrap(targetPlatformProperty(task)));
		};
	}

	private static NativePlatform fromBuildVariant(BuildVariant buildVariant) {
		return NativePlatformFactory.create(((BuildVariantInternal) buildVariant).getAxisValue(TargetMachine.TARGET_MACHINE_COORDINATE_AXIS));
	}

	private static Property<NativePlatform> targetPlatformProperty(Task task) {
		if (task instanceof AbstractLinkTask) {
			return ((AbstractLinkTask) task).getTargetPlatform();
		} else {
			throw new IllegalArgumentException();
		}
	}
	//endregion
}
