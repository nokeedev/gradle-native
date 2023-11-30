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

import dev.nokee.language.base.internal.SourceAwareComponentMixIn;
import dev.nokee.language.nativebase.internal.NativeSourcesAware;
import dev.nokee.model.internal.decorators.NestedObject;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.HasDevelopmentBinary;
import dev.nokee.platform.base.internal.BaseComponent;
import dev.nokee.platform.base.internal.VariantComponentSpec;
import dev.nokee.platform.base.internal.assembletask.AssembleTaskMixIn;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareMixIn;
import dev.nokee.platform.base.internal.mixins.BinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.DependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.TaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.VariantAwareComponentMixIn;
import dev.nokee.platform.jni.JavaNativeInterfaceLibrary;
import dev.nokee.platform.jni.JavaNativeInterfaceLibraryComponentDependencies;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.nativebase.internal.TargetedNativeComponentSpec;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;
import java.util.Set;

public /*final*/ abstract class JniLibraryComponentInternal extends BaseComponent<JniLibrary> implements JavaNativeInterfaceLibrary
	, TargetedNativeComponentSpec
	, NativeSourcesAware
	, ExtensionAwareMixIn
	, VariantComponentSpec<JniLibraryInternal>
	, DependencyAwareComponentMixIn<JavaNativeInterfaceLibraryComponentDependencies>
	, VariantAwareComponentMixIn<JniLibrary>
	, SourceAwareComponentMixIn
	, BinaryAwareComponentMixIn
	, TaskAwareComponentMixIn
	, HasDevelopmentBinary
	, AssembleTaskMixIn
{
	@Inject
	public JniLibraryComponentInternal(ObjectFactory objects) {
		getExtensions().add("baseName", objects.property(String.class));
		getExtensions().add("developmentBinary", objects.property(Binary.class));
	}

	@Override
	@NestedObject
	public abstract DefaultJavaNativeInterfaceLibraryComponentDependencies getDependencies();

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
	public Provider<Set<BuildVariant>> getBuildVariants() {
		return VariantAwareComponentMixIn.super.getBuildVariants();
	}

	@Override
	protected String getTypeName() {
		return "JNI library";
	}
}
