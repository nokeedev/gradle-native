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

import dev.nokee.language.base.internal.SourceComponentSpec;
import dev.nokee.language.nativebase.internal.NativeSourcesAware;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.decorators.NestedObject;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.base.HasDevelopmentBinary;
import dev.nokee.platform.base.internal.DependentComponentSpec;
import dev.nokee.platform.base.internal.ParentAware;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.VariantSpec;
import dev.nokee.platform.base.internal.assembletask.AssembleTaskMixIn;
import dev.nokee.platform.base.internal.mixins.BinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.TaskAwareComponentMixIn;
import dev.nokee.platform.jni.JavaNativeInterfaceNativeComponentDependencies;
import dev.nokee.platform.jni.JniJarBinary;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.internal.NativeSharedLibraryBinarySpec;
import dev.nokee.runtime.nativebase.TargetMachine;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

import static dev.nokee.runtime.nativebase.TargetMachine.TARGET_MACHINE_COORDINATE_AXIS;

public /*final*/ abstract class JniLibraryInternal extends ModelElementSupport implements JniLibrary, VariantInternal
	, VariantSpec
	, NativeSourcesAware
	, TaskAwareComponentMixIn
	, SourceComponentSpec
	, DependentComponentSpec<JavaNativeInterfaceNativeComponentDependencies>
	, BinaryAwareComponentMixIn
	, HasBaseName
	, HasDevelopmentBinary
	, AssembleTaskMixIn
	, ParentAware
{
	@Inject
	public JniLibraryInternal(ObjectFactory objects, ModelObjectRegistry<Task> taskRegistry, ModelObjectRegistry<Artifact> artifactRegistry) {
		getExtensions().add("sharedLibrary", artifactRegistry.register(getIdentifier().child(ElementName.ofMain("sharedLibrary")), NativeSharedLibraryBinarySpec.class).asProvider());
		getExtensions().add("jniJar", artifactRegistry.register(getIdentifier().child(ElementName.ofMain("jniJar")), JniJarBinarySpec.class).asProvider());
	}

	@Override
	public VariantIdentifier getIdentifier() {
		return (VariantIdentifier) super.getIdentifier();
	}

	@Override
	@NestedObject
	public abstract DefaultJavaNativeInterfaceNativeComponentDependencies getDependencies();

	@Override
	public abstract Property<String> getResourcePath();

	public abstract ConfigurableFileCollection getNativeRuntimeFiles();

	@Override
	@SuppressWarnings("unchecked")
	public JniJarBinary getJavaNativeInterfaceJar() {
		return ((NamedDomainObjectProvider<JniJarBinarySpec>) getExtensions().getByName("jniJar")).get();
	}

	public JniJarBinary getJar() {
		return getJavaNativeInterfaceJar();
	}

	@SuppressWarnings("unchecked")
	// Name is main!
//	@Discoverable
	public NativeSharedLibraryBinarySpec getSharedLibrary() {
		return ((NamedDomainObjectProvider<NativeSharedLibraryBinarySpec>) getExtensions().getByName("sharedLibrary")).get();
	}

	@Override
	public void sharedLibrary(Action<? super SharedLibraryBinary> action) {
		action.execute(getSharedLibrary());
	}

	@NestedObject
	public abstract TaskProvider<Task> getSharedLibraryTask();

	@NestedObject
	public abstract TaskProvider<Task> getObjectsTask();

	public TargetMachine getTargetMachine() {
		return getBuildVariant().getAxisValue(TARGET_MACHINE_COORDINATE_AXIS);
	}

	@Override
	protected String getTypeName() {
		return "JNI library";
	}
}
