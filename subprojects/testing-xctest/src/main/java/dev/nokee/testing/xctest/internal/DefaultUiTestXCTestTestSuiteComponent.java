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
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.FullyQualifiedNameComponent;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.BaseNameUtils;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.BinaryName;
import dev.nokee.platform.base.internal.BinaryNamer;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.IsBinary;
import dev.nokee.platform.base.internal.ModelBackedBinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedDependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedNamedMixIn;
import dev.nokee.platform.base.internal.ModelBackedTaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedVariantAwareComponentMixIn;
import dev.nokee.platform.base.internal.TaskNamer;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.ios.internal.IosApplicationBundleInternal;
import dev.nokee.platform.ios.internal.SignedIosApplicationBundleInternal;
import dev.nokee.platform.ios.tasks.internal.CreateIosApplicationBundleTask;
import dev.nokee.platform.ios.tasks.internal.ProcessPropertyListTask;
import dev.nokee.platform.ios.tasks.internal.SignIosApplicationBundleTask;
import dev.nokee.platform.nativebase.BundleBinary;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.BundleBinaryInternal;
import dev.nokee.platform.nativebase.tasks.LinkBundle;
import dev.nokee.testing.xctest.tasks.internal.CreateIosXCTestBundleTask;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Task;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelType.of;

public final class DefaultUiTestXCTestTestSuiteComponent extends BaseXCTestTestSuiteComponent implements Component
	, ModelBackedDependencyAwareComponentMixIn<NativeComponentDependencies>
	, ModelBackedVariantAwareComponentMixIn<DefaultXCTestTestSuiteVariant>
	, ModelBackedBinaryAwareComponentMixIn
	, ModelBackedTaskAwareComponentMixIn
	, ModelBackedNamedMixIn
{
	private final ProviderFactory providers;
	private final TaskRegistry taskRegistry;
	private final ProjectLayout layout;
	private final ModelRegistry registry;

	public DefaultUiTestXCTestTestSuiteComponent(ComponentIdentifier identifier, ObjectFactory objects, ProviderFactory providers, ProjectLayout layout, TaskRegistry taskRegistry, TaskViewFactory taskViewFactory, ModelRegistry registry) {
		super(identifier, objects, providers, layout, taskRegistry, taskViewFactory, registry);
		this.providers = providers;
		this.taskRegistry = taskRegistry;
		this.layout = layout;
		this.registry = registry;
	}

	@Override
	protected void onEachVariant(KnownDomainObject<DefaultXCTestTestSuiteVariant> variant) {
		super.onEachVariant(variant);
		val variantIdentifier = (VariantIdentifier<?>) variant.getIdentifier();

		String moduleName = BaseNameUtils.from(variantIdentifier).getAsCamelCase();
		TaskNamer namer = TaskNamer.INSTANCE;

		variant.configure(testSuite -> {
			testSuite.getBinaries().configureEach(BundleBinary.class, binary -> {
				binary.getLinkTask().configure(task -> {
					// TODO: Filter for matching build variant
					task.dependsOn(getTestedComponent().flatMap(testedComponent -> testedComponent.getVariants().map(it -> it.getDevelopmentBinary())));
				});
			});
		});

		// XCTest UI Testing
		val processUiTestPropertyListTask = taskRegistry.register(namer.determineName(TaskIdentifier.of(variantIdentifier, TaskName.of("process", "propertyList"))), ProcessPropertyListTask.class, task -> {
			task.getIdentifier().set(providers.provider(() -> getGroupId().get().get().get() + "." + moduleName));
			task.getModule().set(moduleName);
			task.getSources().from("src/uiTest/resources/Info.plist");
			task.getOutputFile().set(layout.getBuildDirectory().file("ios/uiTest/Info.plist"));
		});

		TaskProvider<CreateIosXCTestBundleTask> createUiTestXCTestBundle = taskRegistry.register(namer.determineName(TaskIdentifier.of(variantIdentifier, "createUiTestXCTestBundle")), CreateIosXCTestBundleTask.class, task -> {
			task.getXCTestBundle().set(layout.getBuildDirectory().file("ios/products/uiTest/" + moduleName + "-Runner-unsigned.xctest"));
			task.getSources().from(processUiTestPropertyListTask.flatMap(it -> it.getOutputFile()));
			task.getSources().from(variant.flatMap(testSuite -> testSuite.getBinaries().withType(BundleBinary.class).getElements().map(binaries -> binaries.stream().map(binary -> binary.getLinkTask().flatMap(LinkBundle::getLinkedFile)).collect(Collectors.toList()))));
		});

		Provider<CommandLineTool> codeSignatureTool = providers.provider(() -> CommandLineTool.of(new File("/usr/bin/codesign")));
		TaskProvider<SignIosApplicationBundleTask> signUiTestXCTestBundle = taskRegistry.register(namer.determineName(TaskIdentifier.of(variantIdentifier, TaskName.of("sign", "xCTestBundle"))), SignIosApplicationBundleTask.class, task -> {
			task.getUnsignedApplicationBundle().set(createUiTestXCTestBundle.flatMap(CreateIosXCTestBundleTask::getXCTestBundle));
			task.getSignedApplicationBundle().set(layout.getBuildDirectory().file("ios/products/uiTest/" + moduleName + ".xctest"));
			task.getCodeSignatureTool().set(codeSignatureTool);
			task.getCodeSignatureTool().disallowChanges();
		});

		TaskProvider<Sync> prepareXctRunner = taskRegistry.register(namer.determineName(TaskIdentifier.of(variantIdentifier, TaskName.of("prepare", "xctRunner"))), Sync.class, task -> {
			task.from(getXCTRunner());
			task.rename("XCTRunner", moduleName + "-Runner");
			task.setDestinationDir(task.getTemporaryDir());
		});

		TaskProvider<CreateIosApplicationBundleTask> createUiTestApplicationBundleTask = taskRegistry.register(namer.determineName(TaskIdentifier.of(variantIdentifier, TaskName.of("create", "launcherApplicationBundle"))), CreateIosApplicationBundleTask.class, task -> {
			task.getApplicationBundle().set(layout.getBuildDirectory().file("ios/products/uiTest/" + moduleName + "-Runner-unsigned.app"));
			task.getSources().from(prepareXctRunner.map(it -> it.getDestinationDir()));
			task.getSources().from(processUiTestPropertyListTask.flatMap(it -> it.getOutputFile()));
			task.getPlugIns().from(signUiTestXCTestBundle.flatMap(SignIosApplicationBundleTask::getSignedApplicationBundle));
			task.getFrameworks().from(getXCTestFrameworks());
			task.getSwiftSupportRequired().set(false);
		});

		val binaryIdentifierApplicationBundle = BinaryIdentifier.of(BinaryName.of("launcherApplicationBundle"), IosApplicationBundleInternal.class, variantIdentifier);
		registry.register(ModelRegistration.builder()
			.withComponent(IsBinary.tag())
			.withComponent(ConfigurableTag.tag())
			.withComponent(binaryIdentifierApplicationBundle)
			.withComponent(new FullyQualifiedNameComponent(BinaryNamer.INSTANCE.determineName(binaryIdentifierApplicationBundle)))
			.withComponent(createdUsing(of(IosApplicationBundleInternal.class), () -> new IosApplicationBundleInternal(createUiTestApplicationBundleTask)))
			.build());

		val signTask = taskRegistry.register(namer.determineName(TaskIdentifier.of(variantIdentifier, TaskName.of("sign", "launcherApplicationBundle"))), SignIosApplicationBundleTask.class, task -> {
			task.getUnsignedApplicationBundle().set(createUiTestApplicationBundleTask.flatMap(CreateIosApplicationBundleTask::getApplicationBundle));
			task.getSignedApplicationBundle().set(layout.getBuildDirectory().file("ios/products/uiTest/" + moduleName + "-Runner.app"));
			task.getCodeSignatureTool().set(codeSignatureTool);
			task.getCodeSignatureTool().disallowChanges();
		});

		val binaryIdentifierSignedApplicationBundle = BinaryIdentifier.of(BinaryName.of("signedLauncherApplicationBundle"), SignedIosApplicationBundleInternal.class, variantIdentifier);
		val signedLauncherApplicationBundle = new SignedIosApplicationBundleInternal(signTask);
		registry.register(ModelRegistration.builder()
			.withComponent(IsBinary.tag())
			.withComponent(ConfigurableTag.tag())
			.withComponent(binaryIdentifierSignedApplicationBundle)
			.withComponent(new FullyQualifiedNameComponent(BinaryNamer.INSTANCE.determineName(binaryIdentifierSignedApplicationBundle)))
			.withComponent(createdUsing(of(SignedIosApplicationBundleInternal.class), () -> signedLauncherApplicationBundle))
			.build());

		variant.configure(it -> it.getDevelopmentBinary().set(signedLauncherApplicationBundle));

		variant.configure(testSuite -> {
			testSuite.getBinaries().configureEach(BundleBinary.class, binary -> {
				((BundleBinaryInternal)binary).getBaseName().set(BaseNameUtils.from(variantIdentifier).getAsCamelCase());
			});
		});

		TaskProvider<Task> bundle = taskRegistry.register(TaskIdentifier.of(TaskName.of("bundle"), variantIdentifier), task -> {
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
}
