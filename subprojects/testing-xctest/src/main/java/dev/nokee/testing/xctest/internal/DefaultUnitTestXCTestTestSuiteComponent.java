/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.testing.xctest.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.internal.Factory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.language.base.internal.SourceViewAdapter;
import dev.nokee.language.nativebase.internal.NativeSourcesAware;
import dev.nokee.language.objectivec.internal.plugins.SupportObjectiveCSourceSetTag;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.BaseNameUtils;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.assembletask.AssembleTaskMixIn;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareMixIn;
import dev.nokee.platform.base.internal.mixins.BinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.DependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.SourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.TaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.VariantAwareComponentMixIn;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.ios.internal.IosApplicationBundleInternal;
import dev.nokee.platform.ios.internal.SignedIosApplicationBundleInternal;
import dev.nokee.platform.ios.tasks.internal.CreateIosApplicationBundleTask;
import dev.nokee.platform.ios.tasks.internal.ProcessPropertyListTask;
import dev.nokee.platform.ios.tasks.internal.SignIosApplicationBundleTask;
import dev.nokee.platform.nativebase.BundleBinary;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.BundleBinaryInternal;
import dev.nokee.platform.nativebase.internal.TargetBuildTypeAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.TargetLinkageAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.TargetMachineAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.testing.xctest.tasks.internal.CreateIosXCTestBundleTask;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Task;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public /*final*/ abstract class DefaultUnitTestXCTestTestSuiteComponent extends BaseXCTestTestSuiteComponent implements Component
	, NativeSourcesAware
	, ExtensionAwareMixIn
	, DependencyAwareComponentMixIn<NativeComponentDependencies, DefaultNativeComponentDependencies>
	, VariantAwareComponentMixIn<DefaultXCTestTestSuiteVariant>
	, SourceAwareComponentMixIn<SourceView<LanguageSourceSet>, SourceViewAdapter<LanguageSourceSet>>
	, BinaryAwareComponentMixIn
	, TaskAwareComponentMixIn
	, AssembleTaskMixIn
	, HasDevelopmentVariant<DefaultXCTestTestSuiteVariant>
	, TargetMachineAwareComponentMixIn
	, TargetBuildTypeAwareComponentMixIn
	, TargetLinkageAwareComponentMixIn
{
	private final ProviderFactory providers;
	private final ProjectLayout layout;
	private final ModelObjectRegistry<Task> taskRegistry;
	private final ModelObjectRegistry<Artifact> artifactRegistry;

	@Inject
	public DefaultUnitTestXCTestTestSuiteComponent(ObjectFactory objects, ProviderFactory providers, ProjectLayout layout, ModelObjectRegistry<Task> taskRegistry, Factory<SourceView<LanguageSourceSet>> sourcesFactory, ModelObjectRegistry<Artifact> artifactRegistry) {
		super(objects, providers, layout, taskRegistry);
		getExtensions().add("assembleTask", taskRegistry.register(getIdentifier().child(TaskName.of("assemble")), Task.class).asProvider());
		getExtensions().add("sources", sourcesFactory.create());
		getExtensions().create("$objectiveCSupport", SupportObjectiveCSourceSetTag.class);
		this.taskRegistry = taskRegistry;
		this.artifactRegistry = artifactRegistry;
		this.providers = providers;
		this.layout = layout;
	}

	@Override
	protected void onEachVariant(KnownDomainObject<DefaultXCTestTestSuiteVariant> variant) {
		super.onEachVariant(variant);
		val variantIdentifier = (VariantIdentifier) variant.getIdentifier();

		String moduleName = BaseNameUtils.from(variantIdentifier).getAsCamelCase();

		// XCTest Unit Testing
		val processUnitTestPropertyListTask = taskRegistry.register(variantIdentifier.child(TaskName.of("process", "propertyList")), ProcessPropertyListTask.class).configure(task -> {
			task.getIdentifier().set(providers.provider(() -> getGroupId().get().get().get() + "." + moduleName));
			task.getModule().set(moduleName);
			task.getSources().from("src/unitTest/resources/Info.plist");
			task.getOutputFile().set(layout.getBuildDirectory().file("ios/unitTest/Info.plist"));
		}).asProvider();

		val createUnitTestXCTestBundle = taskRegistry.register(variantIdentifier.child(TaskName.of("create", "xCTestBundle")), CreateIosXCTestBundleTask.class).configure(task -> {
			task.getXCTestBundle().set(layout.getBuildDirectory().file("ios/products/unitTest/" + moduleName + "-unsigned.xctest"));
			task.getSources().from(processUnitTestPropertyListTask.flatMap(it -> it.getOutputFile()));
			task.getSources().from(variant.flatMap(testSuite -> testSuite.getBinaries().withType(BundleBinary.class).getElements().map(binaries -> binaries.stream().map(binary -> binary.getLinkTask().get().getLinkedFile()).collect(Collectors.toList()))));
		}).asProvider();
		Provider<CommandLineTool> codeSignatureTool = providers.provider(() -> CommandLineTool.of(new File("/usr/bin/codesign")));
		val signUnitTestXCTestBundle = taskRegistry.register(variantIdentifier.child(TaskName.of("sign", "xCTestBundle")), SignIosApplicationBundleTask.class).configure(task -> {
			task.getUnsignedApplicationBundle().set(createUnitTestXCTestBundle.flatMap(CreateIosXCTestBundleTask::getXCTestBundle));
			task.getSignedApplicationBundle().set(layout.getBuildDirectory().file("ios/products/unitTest/" + moduleName + ".xctest"));
			task.getCodeSignatureTool().set(codeSignatureTool);
			task.getCodeSignatureTool().disallowChanges();
		}).asProvider();

		val binaryIdentifierXCTestBundle = variantIdentifier.child("unitTestXCTestBundle");
		// TODO: register `createUnitTestXCTestBundle` inside IosXCTestBundle
		final Provider<IosXCTestBundle> xcTestBundle = artifactRegistry.register(binaryIdentifierXCTestBundle, IosXCTestBundle.class).asProvider();
		// We could use signed bundle as development binary but right now it's only used in Xcode which Xcode will perform the signing so no need to provide a signed bundle
		variant.configure(it -> it.getDevelopmentBinary().set(xcTestBundle));

		val createUnitTestApplicationBundleTask = taskRegistry.register(variantIdentifier.child(TaskName.of("create", "launcherApplicationBundle")), CreateIosApplicationBundleTask.class).configure(task -> {
			task.getApplicationBundle().set(layout.getBuildDirectory().file("ios/products/unitTest/" + getTestedComponent().get().getBaseName().get() + "-unsigned.app"));
			task.getSources().from(getTestedComponent().flatMap(c -> c.getVariants().getElements().map(it -> it.iterator().next().getBinaries().withType(IosApplicationBundleInternal.class).get().iterator().next().getBundleTask().map(t -> t.getSources()))));
			task.getPlugIns().from(signUnitTestXCTestBundle.flatMap(SignIosApplicationBundleTask::getSignedApplicationBundle));
			task.getFrameworks().from(getXCTestBundleInjectDynamicLibrary());
			task.getFrameworks().from(getXCTestFrameworks());
			task.getSwiftSupportRequired().set(false);
		}).asProvider();

		val signTask = taskRegistry.register(variantIdentifier.child(TaskName.of("sign", "launcherApplicationBundle")), SignIosApplicationBundleTask.class).configure(task -> {
			task.getUnsignedApplicationBundle().set(createUnitTestApplicationBundleTask.flatMap(CreateIosApplicationBundleTask::getApplicationBundle));
			task.getSignedApplicationBundle().set(layout.getBuildDirectory().file("ios/products/unitTest/" + getTestedComponent().get().getBaseName().get() + ".app"));
			task.getCodeSignatureTool().set(codeSignatureTool);
			task.getCodeSignatureTool().disallowChanges();
		}).asProvider();

		val binaryIdentifierApplicationBundle = variantIdentifier.child("signedApplicationBundle");
		// TODO: register `signTask` inside SignedIosApplicationBundleInternal
		artifactRegistry.register(binaryIdentifierApplicationBundle, SignedIosApplicationBundleInternal.class);

		variant.configure(testSuite -> {
			testSuite.getBinaries().configureEach(BundleBinary.class, binary -> {
				((BundleBinaryInternal)binary).getBaseName().set(BaseNameUtils.from(variantIdentifier).getAsCamelCase());
			});
		});

		taskRegistry.register(variantIdentifier.child(TaskName.of("bundle")), Task.class).configure(task -> {
			task.dependsOn(variant.map(it -> it.getBinaries().withType(SignedIosApplicationBundleInternal.class).get()));
		});
	}

	private Provider<File> getXCTestBundleInjectDynamicLibrary() {
		return providers.provider(() -> new File(getSdkPlatformPath(), "Developer/usr/lib/libXCTestBundleInject.dylib"));
	}

	private Provider<List<File>> getXCTestFrameworks() {
		return providers.provider(() -> {
			return ImmutableList.<File>builder()
				.add(new File(getSdkPlatformPath(), "Developer/Library/PrivateFrameworks/XCTAutomationSupport.framework"))
				.add(new File(getSdkPlatformPath(), "Developer/usr/lib/libXCTestSwiftSupport.dylib"))
				.add(new File(getSdkPlatformPath(), "Developer/Library/Frameworks/XCTest.framework"))
				.build();
		});
	}

	public static String getSdkPlatformPath() {
		try {
			Process process = new ProcessBuilder("xcrun", "--sdk", "iphonesimulator", "--show-sdk-platform-path").start();
			process.waitFor();
			return IOUtils.toString(process.getInputStream(), Charset.defaultCharset()).trim();
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public abstract Property<DefaultXCTestTestSuiteVariant> getDevelopmentVariant();

	@Override
	public VariantView<DefaultXCTestTestSuiteVariant> getVariants() {
		return VariantAwareComponentMixIn.super.getVariants();
	}

	@Override
	public Provider<Set<BuildVariant>> getBuildVariants() {
		return VariantAwareComponentMixIn.super.getBuildVariants();
	}

	@Override
	protected String getTypeName() {
		return "XCTest test suite";
	}
}
