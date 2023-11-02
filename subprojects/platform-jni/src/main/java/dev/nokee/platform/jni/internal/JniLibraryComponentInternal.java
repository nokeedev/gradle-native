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

import dev.nokee.language.nativebase.internal.NativeSourcesAwareTag;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.HasDevelopmentBinary;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.BaseComponent;
import dev.nokee.platform.base.internal.DependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.platform.base.internal.IsComponent;
import dev.nokee.platform.base.internal.ModelBackedBinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedSourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedTaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedVariantAwareComponentMixIn;
import dev.nokee.platform.base.internal.assembletask.HasAssembleTaskMixIn;
import dev.nokee.platform.base.internal.developmentvariant.HasDevelopmentVariantMixIn;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareMixIn;
import dev.nokee.platform.jni.JavaNativeInterfaceLibrary;
import dev.nokee.platform.jni.JavaNativeInterfaceLibraryComponentDependencies;
import dev.nokee.platform.jni.JavaNativeInterfaceLibrarySources;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetLinkageAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.ModelBackedTargetMachineAwareComponentMixIn;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;
import java.util.Set;

@DomainObjectEntities.Tag({IsComponent.class, NativeSourcesAwareTag.class})
public /*final*/ abstract class JniLibraryComponentInternal extends BaseComponent<JniLibrary> implements JavaNativeInterfaceLibrary
	, ExtensionAwareMixIn
	, DependencyAwareComponentMixIn<JavaNativeInterfaceLibraryComponentDependencies>
	, ModelBackedVariantAwareComponentMixIn<JniLibrary>
	, ModelBackedSourceAwareComponentMixIn<JavaNativeInterfaceLibrarySources, JavaNativeInterfaceSourcesViewAdapter>
	, ModelBackedBinaryAwareComponentMixIn
	, ModelBackedTaskAwareComponentMixIn
	, ModelBackedTargetMachineAwareComponentMixIn
	, ModelBackedTargetLinkageAwareComponentMixIn
	, HasDevelopmentVariantMixIn<JniLibrary>
	, HasAssembleTaskMixIn
	, HasDevelopmentBinary
{
	@Inject
	public JniLibraryComponentInternal(ModelObjectRegistry<DependencyBucket> bucketRegistry, ObjectFactory objects) {
		getExtensions().create("dependencies", DefaultJavaNativeInterfaceLibraryComponentDependencies.class, getIdentifier(), bucketRegistry);
		getExtensions().add("baseName", objects.property(String.class));
		getExtensions().add("developmentBinary", objects.property(Binary.class));
		getExtensions().add("developmentVariant", objects.property(JniLibrary.class));
	}

	@Override
	public DefaultJavaNativeInterfaceLibraryComponentDependencies getDependencies() {
		return (DefaultJavaNativeInterfaceLibraryComponentDependencies) DependencyAwareComponentMixIn.super.getDependencies();
	}

	public VariantView<JniLibrary> getVariants() {
		return ModelBackedVariantAwareComponentMixIn.super.getVariants();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Property<String> getBaseName() {
		return (Property<String>) getExtensions().getByName("baseName");
	}

	@Override
	@SuppressWarnings("unchecked")
	public Property<Binary> getDevelopmentBinary() {
		return (Property<Binary>) getExtensions().getByName("developmentBinary");
	}

	@Override
	@SuppressWarnings("unchecked")
	public Property<JniLibrary> getDevelopmentVariant() {
		return (Property<JniLibrary>) getExtensions().getByName("developmentVariant");
	}

	@Override
	@SuppressWarnings("unchecked")
	public BinaryView<Binary> getBinaries() {
		return ModelProperties.getProperty(this, "binaries").as(BinaryView.class).get();
	}

	@Override
	public Provider<Set<BuildVariant>> getBuildVariants() {
		return ModelBackedVariantAwareComponentMixIn.super.getBuildVariants();
	}

	public String toString() {
		return "JNI library '" + getName() + "'";
	}
}
