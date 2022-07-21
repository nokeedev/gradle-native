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
package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.HasConfigurableSource;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.c.internal.plugins.CSourceSetSpec;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.cpp.internal.plugins.CppSourceSetSpec;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.nativebase.internal.HasConfigurableHeaders;
import dev.nokee.language.nativebase.internal.NativeLanguageSourceSetAwareTag;
import dev.nokee.language.nativebase.internal.NativePlatformFactory;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCSourceSetSpec;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppSourceSetSpec;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.language.swift.internal.plugins.SwiftSourceSetSpec;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ExcludeFromQualifyingNameTag;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BaseComponent;
import dev.nokee.platform.base.internal.BaseNameComponent;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.BinaryIdentity;
import dev.nokee.platform.base.internal.BuildVariantComponent;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.IsBinary;
import dev.nokee.platform.base.internal.VariantAwareComponentInternal;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.TargetMachine;
import lombok.val;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.util.GUtil;

import java.util.concurrent.Callable;

import static dev.nokee.model.internal.actions.ModelAction.configureEach;
import static dev.nokee.model.internal.actions.ModelSpec.descendantOf;
import static dev.nokee.model.internal.actions.ModelSpec.subtypeOf;
import static dev.nokee.model.internal.core.ModelNodeUtils.canBeViewedAs;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.runtime.nativebase.TargetMachine.TARGET_MACHINE_COORDINATE_AXIS;

public abstract class BaseNativeComponent<T extends Variant> extends BaseComponent<T> implements VariantAwareComponentInternal<T> {
	private final ObjectFactory objects;
	private final ModelRegistry registry;
	private final ProviderFactory providers;

	public BaseNativeComponent(ComponentIdentifier identifier, ObjectFactory objects, TaskRegistry taskRegistry, ModelRegistry registry, ProviderFactory providers) {
		super(identifier);
		this.objects = objects;
		this.registry = registry;
		this.providers = providers;
	}

	public abstract NativeComponentDependencies getDependencies();

	protected void createBinaries(KnownDomainObject<T> knownVariant) {
		doCreateBinaries((VariantIdentifier) knownVariant.getIdentifier(), knownVariant);
	}

	private void doCreateBinaries(VariantIdentifier variantIdentifier, KnownDomainObject<T> knownVariant) {
		val buildVariant = (BuildVariantInternal) variantIdentifier.getBuildVariant();
		final TargetMachine targetMachineInternal = buildVariant.getAxisValue(TARGET_MACHINE_COORDINATE_AXIS);

		if (buildVariant.hasAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS)) {
			registry.instantiate(configureEach(descendantOf(ModelNodes.of(knownVariant).getId()).and(subtypeOf(of(HasConfigurableHeaders.class))), LanguageSourceSet.class, sourceSet -> {
				((HasConfigurableHeaders) sourceSet).getHeaders().setFrom((Callable<?>) () -> {
					return ((ModelLookup) registry).query(entity -> canBeViewedAs(entity, of(NativeHeaderSet.class)) && entity.find(ParentComponent.class).map(it -> it.get().equals(getNode())).orElse(false)).map(it -> ModelNodeUtils.get(it, NativeHeaderSet.class).getSourceDirectories());
				});
			}));
			registry.instantiate(configureEach(descendantOf(ModelNodes.of(knownVariant).getId()), CSourceSetSpec.class, sourceSet -> {
				((HasConfigurableSource) sourceSet).getSource().setFrom((Callable<?>) () -> ((ModelLookup) registry).query(entity -> canBeViewedAs(entity, of(CSourceSet.class)) && entity.find(ParentComponent.class).map(it -> it.get().equals(getNode())).orElse(false)).map(it -> ModelNodeUtils.get(it, CSourceSet.class).getAsFileTree()));
				sourceSet.getCompileTask().configure(task -> NativePlatformFactory.create(buildVariant).ifPresent(task.getTargetPlatform()::set));
				ModelNodes.of(sourceSet).addComponent(new BuildVariantComponent(buildVariant));
			}));
			registry.instantiate(configureEach(descendantOf(ModelNodes.of(knownVariant).getId()), CppSourceSetSpec.class, sourceSet -> {
				((HasConfigurableSource) sourceSet).getSource().setFrom((Callable<?>) () -> ((ModelLookup) registry).query(entity -> canBeViewedAs(entity, of(CppSourceSet.class)) && entity.find(ParentComponent.class).map(it -> it.get().equals(getNode())).orElse(false)).map(it -> ModelNodeUtils.get(it, CppSourceSet.class).getAsFileTree()));
				sourceSet.getCompileTask().configure(task -> NativePlatformFactory.create(buildVariant).ifPresent(task.getTargetPlatform()::set));
				ModelNodes.of(sourceSet).addComponent(new BuildVariantComponent(buildVariant));
			}));
			registry.instantiate(configureEach(descendantOf(ModelNodes.of(knownVariant).getId()), ObjectiveCSourceSetSpec.class, sourceSet -> {
				((HasConfigurableSource) sourceSet).getSource().setFrom((Callable<?>) () -> ((ModelLookup) registry).query(entity -> canBeViewedAs(entity, of(ObjectiveCSourceSet.class)) && entity.find(ParentComponent.class).map(it -> it.get().equals(getNode())).orElse(false)).map(it -> ModelNodeUtils.get(it, ObjectiveCSourceSet.class).getAsFileTree()));
				sourceSet.getCompileTask().configure(task -> NativePlatformFactory.create(buildVariant).ifPresent(task.getTargetPlatform()::set));
				ModelNodes.of(sourceSet).addComponent(new BuildVariantComponent(buildVariant));
			}));
			registry.instantiate(configureEach(descendantOf(ModelNodes.of(knownVariant).getId()), ObjectiveCppSourceSetSpec.class, sourceSet -> {
				((HasConfigurableSource) sourceSet).getSource().setFrom((Callable<?>) () -> ((ModelLookup) registry).query(entity -> canBeViewedAs(entity, of(ObjectiveCppSourceSet.class)) && entity.find(ParentComponent.class).map(it -> it.get().equals(getNode())).orElse(false)).map(it -> ModelNodeUtils.get(it, ObjectiveCppSourceSet.class).getAsFileTree()));
				sourceSet.getCompileTask().configure(task -> NativePlatformFactory.create(buildVariant).ifPresent(task.getTargetPlatform()::set));
				ModelNodes.of(sourceSet).addComponent(new BuildVariantComponent(buildVariant));
			}));
			registry.instantiate(configureEach(descendantOf(ModelNodes.of(knownVariant).getId()), SwiftSourceSetSpec.class, sourceSet -> {
				((HasConfigurableSource) sourceSet).getSource().setFrom((Callable<?>) () -> ((ModelLookup) registry).query(entity -> canBeViewedAs(entity, of(SwiftSourceSet.class)) && entity.find(ParentComponent.class).map(it -> it.get().equals(getNode())).orElse(false)).map(it -> ModelNodeUtils.get(it, SwiftSourceSet.class).getAsFileTree()));
				sourceSet.getCompileTask().configure(task -> NativePlatformFactory.create(buildVariant).ifPresent(task.getTargetPlatform()::set));
				sourceSet.getCompileTask().configure(task -> task.getModuleName().set(providers.provider(() -> GUtil.toCamelCase(ModelStates.finalize(ModelNodes.of(sourceSet).get(ParentComponent.class).get()).get(BaseNameComponent.class).get()))));
				ModelNodes.of(sourceSet).addComponent(new BuildVariantComponent(buildVariant));
			}));

			val linkage = buildVariant.getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS);
			if (linkage.isExecutable()) {
				val binaryIdentifier = BinaryIdentifier.of(variantIdentifier, BinaryIdentity.ofMain("executable", "executable binary"));

				registry.register(ModelRegistration.builder()
					.withComponent(tag(IsBinary.class))
					.withComponent(tag(ConfigurableTag.class))
					.withComponent(tag(ExcludeFromQualifyingNameTag.class))
					.withComponent(tag(NativeLanguageSourceSetAwareTag.class))
					.withComponent(new IdentifierComponent(binaryIdentifier))
					.withComponent(new DisplayNameComponent("executable binary"))
					.withComponent(new BuildVariantComponent(buildVariant))
					.withComponent(createdUsing(of(ExecutableBinaryInternal.class), () -> {
						return objects.newInstance(ExecutableBinaryInternal.class, binaryIdentifier, targetMachineInternal);
					}))
					.build());
			} else if (linkage.isShared()) {
				val binaryIdentifier = BinaryIdentifier.of(variantIdentifier, BinaryIdentity.ofMain("sharedLibrary", "shared library binary"));

				registry.register(ModelRegistration.builder()
					.withComponent(tag(IsBinary.class))
					.withComponent(tag(ConfigurableTag.class))
					.withComponent(tag(ExcludeFromQualifyingNameTag.class))
					.withComponent(tag(NativeLanguageSourceSetAwareTag.class))
					.withComponent(new IdentifierComponent(binaryIdentifier))
					.withComponent(new DisplayNameComponent("shared library binary"))
					.withComponent(new BuildVariantComponent(buildVariant))
					.withComponent(createdUsing(of(SharedLibraryBinaryInternal.class), () -> {
						return objects.newInstance(SharedLibraryBinaryInternal.class, binaryIdentifier, targetMachineInternal);
					}))
					.build());
			} else if (linkage.isBundle()) {
				val binaryIdentifier = BinaryIdentifier.of(variantIdentifier, BinaryIdentity.ofMain("bundle", "bundle binary"));

				registry.register(ModelRegistration.builder()
					.withComponent(tag(IsBinary.class))
					.withComponent(tag(ConfigurableTag.class))
					.withComponent(tag(ExcludeFromQualifyingNameTag.class))
					.withComponent(tag(NativeLanguageSourceSetAwareTag.class))
					.withComponent(new IdentifierComponent(binaryIdentifier))
					.withComponent(new DisplayNameComponent("bundle binary"))
					.withComponent(new BuildVariantComponent(buildVariant))
					.withComponent(createdUsing(of(BundleBinaryInternal.class), () -> {
						return objects.newInstance(BundleBinaryInternal.class, binaryIdentifier, targetMachineInternal);
					}))
					.build());
			} else if (linkage.isStatic()) {
				val binaryIdentifier = BinaryIdentifier.of(variantIdentifier, BinaryIdentity.ofMain("staticLibrary", "static library binary"));

				registry.register(ModelRegistration.builder()
					.withComponent(tag(IsBinary.class))
					.withComponent(tag(ConfigurableTag.class))
					.withComponent(tag(ExcludeFromQualifyingNameTag.class))
					.withComponent(tag(NativeLanguageSourceSetAwareTag.class))
					.withComponent(new IdentifierComponent(binaryIdentifier))
					.withComponent(new DisplayNameComponent("static library binary"))
					.withComponent(new BuildVariantComponent(buildVariant))
					.withComponent(createdUsing(of(StaticLibraryBinaryInternal.class), () -> {
						return objects.newInstance(StaticLibraryBinaryInternal.class, binaryIdentifier, targetMachineInternal);
					}))
					.build());
			}
		}
	}
}
