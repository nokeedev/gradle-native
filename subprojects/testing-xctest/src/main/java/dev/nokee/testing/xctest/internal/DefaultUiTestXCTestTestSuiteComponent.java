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
import dev.nokee.model.internal.decorators.NestedObject;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.internal.BaseNameUtils;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.assembletask.AssembleTaskMixIn;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareMixIn;
import dev.nokee.platform.base.internal.mixins.BinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.DependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.SourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.TaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.VariantAwareComponentMixIn;
import dev.nokee.platform.ios.internal.IosApplicationBundleInternal;
import dev.nokee.platform.ios.internal.SignedIosApplicationBundleInternal;
import dev.nokee.platform.ios.tasks.internal.CreateIosApplicationBundleTask;
import dev.nokee.platform.ios.tasks.internal.ProcessPropertyListTask;
import dev.nokee.platform.ios.tasks.internal.SignIosApplicationBundleTask;
import dev.nokee.platform.nativebase.BundleBinary;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.platform.nativebase.internal.NativeBundleBinarySpec;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.platform.nativebase.tasks.LinkBundle;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.testing.xctest.tasks.internal.CreateIosXCTestBundleTask;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Task;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.Sync;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public /*final*/ abstract class DefaultUiTestXCTestTestSuiteComponent extends BaseXCTestTestSuiteComponent implements TestSuiteComponent
	, NativeSourcesAware
	, ExtensionAwareMixIn
	, DependencyAwareComponentMixIn<NativeComponentDependencies>
	, VariantAwareComponentMixIn<DefaultXCTestTestSuiteVariant>
	, SourceAwareComponentMixIn<SourceView<LanguageSourceSet>, SourceViewAdapter<LanguageSourceSet>>
	, BinaryAwareComponentMixIn
	, TaskAwareComponentMixIn
	, AssembleTaskMixIn
	, HasDevelopmentVariant<DefaultXCTestTestSuiteVariant>
{
	private final ProviderFactory providers;
	private final ProjectLayout layout;
	private final ModelObjectRegistry<Task> taskRegistry;
	private final ModelObjectRegistry<Artifact> artifactRegistry;

	@Inject
	public DefaultUiTestXCTestTestSuiteComponent(ObjectFactory objects, ProviderFactory providers, ProjectLayout layout, ModelObjectRegistry<Task> taskRegistry, Factory<SourceView<LanguageSourceSet>> sourcesFactory, ModelObjectRegistry<Artifact> artifactRegistry) {
		super(objects, providers, layout, taskRegistry);
		getExtensions().add("sources", sourcesFactory.create());
		getExtensions().create("$objectiveCSupport", SupportObjectiveCSourceSetTag.class);
		this.artifactRegistry = artifactRegistry;
		this.taskRegistry = taskRegistry;
		this.providers = providers;
		this.layout = layout;
	}

	@Override
	@NestedObject
	public abstract DefaultNativeComponentDependencies getDependencies();

	@Override
	protected void onEachVariant(KnownDomainObject<DefaultXCTestTestSuiteVariant> variant) {
		super.onEachVariant(variant);
		val variantIdentifier = (VariantIdentifier) variant.getIdentifier();

		String moduleName = BaseNameUtils.from(variantIdentifier).getAsCamelCase();

		variant.configure(testSuite -> {
			testSuite.getBinaries().configureEach(BundleBinary.class, binary -> {
				binary.getLinkTask().configure(task -> {
					// TODO: Filter for matching build variant
					task.dependsOn(getTestedComponent().map(it -> ((BaseNativeComponent<?>) it)).flatMap(testedComponent -> testedComponent.getVariants().map(it -> it.getDevelopmentBinary())));
				});
			});
		});

		// XCTest UI Testing
		val processUiTestPropertyListTask = taskRegistry.register(variantIdentifier.child(TaskName.of("process", "propertyList")), ProcessPropertyListTask.class).configure(task -> {
			task.getIdentifier().set(providers.provider(() -> getGroupId().get().get().get() + "." + moduleName));
			task.getModule().set(moduleName);
			task.getSources().from("src/uiTest/resources/Info.plist");
			task.getOutputFile().set(layout.getBuildDirectory().file("ios/uiTest/Info.plist"));
		}).asProvider();

		val createUiTestXCTestBundle = taskRegistry.register(variantIdentifier.child(TaskName.of("createUiTestXCTestBundle")), CreateIosXCTestBundleTask.class).configure(task -> {
			task.getXCTestBundle().set(layout.getBuildDirectory().file("ios/products/uiTest/" + moduleName + "-Runner-unsigned.xctest"));
			task.getSources().from(processUiTestPropertyListTask.flatMap(it -> it.getOutputFile()));
			task.getSources().from(variant.flatMap(testSuite -> testSuite.getBinaries().withType(BundleBinary.class).getElements().map(binaries -> binaries.stream().map(binary -> binary.getLinkTask().flatMap(LinkBundle::getLinkedFile)).collect(Collectors.toList()))));
		}).asProvider();

		Provider<CommandLineTool> codeSignatureTool = providers.provider(() -> CommandLineTool.of(new File("/usr/bin/codesign")));
		val signUiTestXCTestBundle = taskRegistry.register(variantIdentifier.child(TaskName.of("sign", "xCTestBundle")), SignIosApplicationBundleTask.class).configure(task -> {
			task.getUnsignedApplicationBundle().set(createUiTestXCTestBundle.flatMap(CreateIosXCTestBundleTask::getXCTestBundle));
			task.getSignedApplicationBundle().set(layout.getBuildDirectory().file("ios/products/uiTest/" + moduleName + ".xctest"));
			task.getCodeSignatureTool().set(codeSignatureTool);
			task.getCodeSignatureTool().disallowChanges();
		}).asProvider();

		val prepareXctRunner = taskRegistry.register(variantIdentifier.child(TaskName.of("prepare", "xctRunner")), Sync.class).configure(task -> {
			task.from(getXCTRunner());
			task.rename("XCTRunner", moduleName + "-Runner");
			task.setDestinationDir(task.getTemporaryDir());
		}).asProvider();

		val createUiTestApplicationBundleTask = taskRegistry.register(variantIdentifier.child(TaskName.of("create", "launcherApplicationBundle")), CreateIosApplicationBundleTask.class).configure(task -> {
			task.getApplicationBundle().set(layout.getBuildDirectory().file("ios/products/uiTest/" + moduleName + "-Runner-unsigned.app"));
			task.getSources().from(prepareXctRunner.map(it -> it.getDestinationDir()));
			task.getSources().from(processUiTestPropertyListTask.flatMap(it -> it.getOutputFile()));
			task.getPlugIns().from(signUiTestXCTestBundle.flatMap(SignIosApplicationBundleTask::getSignedApplicationBundle));
			task.getFrameworks().from(getXCTestFrameworks());
			task.getSwiftSupportRequired().set(false);
		}).asProvider();

		val binaryIdentifierApplicationBundle = variantIdentifier.child("launcherApplicationBundle");
		// TODO: register `createUiTestApplicationBundleTask` inside IosApplicationBundleInternal
		artifactRegistry.register(binaryIdentifierApplicationBundle, IosApplicationBundleInternal.class);

		val signTask = taskRegistry.register(variantIdentifier.child(TaskName.of("sign", "launcherApplicationBundle")), SignIosApplicationBundleTask.class).configure(task -> {
			task.getUnsignedApplicationBundle().set(createUiTestApplicationBundleTask.flatMap(CreateIosApplicationBundleTask::getApplicationBundle));
			task.getSignedApplicationBundle().set(layout.getBuildDirectory().file("ios/products/uiTest/" + moduleName + "-Runner.app"));
			task.getCodeSignatureTool().set(codeSignatureTool);
			task.getCodeSignatureTool().disallowChanges();
		}).asProvider();

		val binaryIdentifierSignedApplicationBundle = variantIdentifier.child("signedLauncherApplicationBundle");
		// TODO: register `signTask` inside SignedIosApplicationBundleInternal
		final Provider<SignedIosApplicationBundleInternal> signedLauncherApplicationBundle = artifactRegistry.register(binaryIdentifierSignedApplicationBundle, SignedIosApplicationBundleInternal.class).asProvider();

		variant.configure(it -> it.getDevelopmentBinary().set(signedLauncherApplicationBundle));

		variant.configure(testSuite -> {
			testSuite.getBinaries().configureEach(BundleBinary.class, binary -> {
				((NativeBundleBinarySpec)binary).getBaseName().set(BaseNameUtils.from(variantIdentifier).getAsCamelCase());
			});
		});

		taskRegistry.register(variantIdentifier.child("bundle"), Task.class).configure(task -> {
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

	private Provider<File> getXCTRunner() {
		return providers.provider(() -> {
			return new File(getSdkPlatformPath(), "Developer/Library/Xcode/Agents/XCTRunner.app/XCTRunner");
		});
	}

	@Override
	public abstract Property<DefaultXCTestTestSuiteVariant> getDevelopmentVariant();

	@Override
	public Provider<Set<BuildVariant>> getBuildVariants() {
		return VariantAwareComponentMixIn.super.getBuildVariants();
	}

	@Override
	protected String getTypeName() {
		return "XCTest test suite";
	}
}
