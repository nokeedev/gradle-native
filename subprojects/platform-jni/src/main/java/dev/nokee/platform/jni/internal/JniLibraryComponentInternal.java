/*
 * Copyright 2020-2021 the original author or authors.
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

import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.jni.JavaNativeInterfaceLibrary;
import dev.nokee.platform.jni.JavaNativeInterfaceLibraryComponentDependencies;
import dev.nokee.platform.jni.JavaNativeInterfaceLibrarySources;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetMachineAwareComponentMixIn;
import lombok.Getter;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;
import java.util.Set;

public class JniLibraryComponentInternal extends BaseComponent<JniLibrary> implements JavaNativeInterfaceLibrary
	, ModelBackedDependencyAwareComponentMixIn<JavaNativeInterfaceLibraryComponentDependencies>
	, ModelBackedVariantAwareComponentMixIn<JniLibrary>
	, ModelBackedSourceAwareComponentMixIn<JavaNativeInterfaceLibrarySources>
	, ModelBackedBinaryAwareComponentMixIn
	, ModelBackedTaskAwareComponentMixIn
	, ModelBackedNamedMixIn
	, ModelBackedHasBaseNameMixIn
	, ModelBackedTargetMachineAwareComponentMixIn
	, ModelBackedHasDevelopmentVariantMixIn<JniLibrary>
{
	@Getter private final GroupId groupId;

	@Inject
	public JniLibraryComponentInternal(ComponentIdentifier identifier, GroupId groupId, ObjectFactory objects) {
		super(identifier, objects);
		this.groupId = groupId;
	}

	public VariantView<JniLibrary> getVariants() {
		return ModelBackedVariantAwareComponentMixIn.super.getVariants();
	}

	@Override
	public Property<String> getBaseName() {
		return ModelBackedHasBaseNameMixIn.super.getBaseName();
	}

	@Override
	public Property<JniLibrary> getDevelopmentVariant() {
		return ModelBackedHasDevelopmentVariantMixIn.super.getDevelopmentVariant();
	}

	@Override
	public BinaryView<Binary> getBinaries() {
		return ModelProperties.getProperty(this, "binaries").as(BinaryView.class).get();
	}

	@Override
	public VariantCollection<JniLibrary> getVariantCollection() {
		throw new UnsupportedOperationException("Use 'variants' property instead.");
	}

	@Override
	public Provider<Set<BuildVariant>> getBuildVariants() {
		return ModelBackedVariantAwareComponentMixIn.super.getBuildVariants();
	}
}
