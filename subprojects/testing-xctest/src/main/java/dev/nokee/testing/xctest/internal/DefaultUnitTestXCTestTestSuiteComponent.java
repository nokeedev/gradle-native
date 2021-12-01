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
import dev.nokee.model.internal.DomainObjectCreated;
import dev.nokee.model.internal.DomainObjectDiscovered;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.*;
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
import dev.nokee.testing.xctest.tasks.internal.CreateIosXCTestBundleTask;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

import static dev.nokee.model.internal.core.ModelComponentType.componentOf;

public final class DefaultUnitTestXCTestTestSuiteComponent extends BaseXCTestTestSuiteComponent implements Component
	, ModelBackedDependencyAwareComponentMixIn<NativeComponentDependencies>
	, ModelBackedVariantAwareComponentMixIn<DefaultXCTestTestSuiteVariant>
	, ModelBackedBinaryAwareComponentMixIn
	, ModelBackedTaskAwareComponentMixIn
	, ModelBackedNamedMixIn
{
	private final ProviderFactory providers;
	private final TaskRegistry taskRegistry;
	private final ProjectLayout layout;
	private final DomainObjectEventPublisher eventPublisher;

	public DefaultUnitTestXCTestTestSuiteComponent(ComponentIdentifier identifier, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ProjectLayout layout, DomainObjectEventPublisher eventPublisher, TaskRegistry taskRegistry, TaskViewFactory taskViewFactory) {
		super(identifier, objects, providers, tasks, layout, eventPublisher, taskRegistry, taskViewFactory);
		this.providers = providers;
		this.taskRegistry = taskRegistry;
		this.layout = layout;
		this.eventPublisher = eventPublisher;
	}

	@Override
	protected void onEachVariant(KnownDomainObject<DefaultXCTestTestSuiteVariant> variant) {
		super.onEachVariant(variant);
		val variantIdentifier = ModelNodes.of(variant).getComponent(componentOf(VariantIdentifier.class));

		String moduleName = BaseNameUtils.from(variantIdentifier).getAsCamelCase();
		TaskNamer namer = TaskNamer.INSTANCE;

		// XCTest Unit Testing
		val processUnitTestPropertyListTask = taskRegistry.register(namer.determineName(TaskIdentifier.of(variantIdentifier, TaskName.of("process", "propertyList"))), ProcessPropertyListTask.class, task -> {
			task.getIdentifier().set(providers.provider(() -> getGroupId().get().get().get() + "." + moduleName));
			task.getModule().set(moduleName);
			task.getSources().from("src/unitTest/resources/Info.plist");
			task.getOutputFile().set(layout.getBuildDirectory().file("ios/unitTest/Info.plist"));
		});

		val createUnitTestXCTestBundle = taskRegistry.register(namer.determineName(TaskIdentifier.of(variantIdentifier, TaskName.of("create", "xCTestBundle"))), CreateIosXCTestBundleTask.class, task -> {
			task.getXCTestBundle().set(layout.getBuildDirectory().file("ios/products/unitTest/" + moduleName + "-unsigned.xctest"));
			task.getSources().from(processUnitTestPropertyListTask.flatMap(it -> it.getOutputFile()));
			task.getSources().from(variant.flatMap(testSuite -> testSuite.getBinaries().withType(BundleBinary.class).getElements().map(binaries -> binaries.stream().map(binary -> binary.getLinkTask().get().getLinkedFile()).collect(Collectors.toList()))));
		});
		Provider<CommandLineTool> codeSignatureTool = providers.provider(() -> CommandLineTool.of(new File("/usr/bin/codesign")));
		val signUnitTestXCTestBundle = taskRegistry.register(namer.determineName(TaskIdentifier.of(variantIdentifier, TaskName.of("sign", "xCTestBundle"))), SignIosApplicationBundleTask.class, task -> {
			task.getUnsignedApplicationBundle().set(createUnitTestXCTestBundle.flatMap(CreateIosXCTestBundleTask::getXCTestBundle));
			task.getSignedApplicationBundle().set(layout.getBuildDirectory().file("ios/products/unitTest/" + moduleName + ".xctest"));
			task.getCodeSignatureTool().set(codeSignatureTool);
			task.getCodeSignatureTool().disallowChanges();
		});

		val binaryIdentifierXCTestBundle = BinaryIdentifier.of(BinaryName.of("unitTestXCTestBundle"), IosXCTestBundle.class, variantIdentifier);
		eventPublisher.publish(new DomainObjectDiscovered<>(binaryIdentifierXCTestBundle));
		val xcTestBundle = new IosXCTestBundle(createUnitTestXCTestBundle);
		eventPublisher.publish(new DomainObjectCreated<>(binaryIdentifierXCTestBundle, xcTestBundle));
		// We could use signed bundle as development binary but right now it's only used in Xcode which Xcode will perform the signing so no need to provide a signed bundle
		variant.configure(it -> it.getDevelopmentBinary().set(xcTestBundle));

		val createUnitTestApplicationBundleTask = taskRegistry.register(namer.determineName(TaskIdentifier.of(variantIdentifier, TaskName.of("create", "launcherApplicationBundle"))), CreateIosApplicationBundleTask.class, task -> {
			task.getApplicationBundle().set(layout.getBuildDirectory().file("ios/products/unitTest/" + getTestedComponent().get().getBaseName().get() + "-unsigned.app"));
			task.getSources().from(getTestedComponent().flatMap(c -> c.getVariants().getElements().map(it -> it.iterator().next().getBinaries().withType(IosApplicationBundleInternal.class).get().iterator().next().getBundleTask().map(t -> t.getSources()))));
			task.getPlugIns().from(signUnitTestXCTestBundle.flatMap(SignIosApplicationBundleTask::getSignedApplicationBundle));
			task.getFrameworks().from(getXCTestBundleInjectDynamicLibrary());
			task.getFrameworks().from(getXCTestFrameworks());
			task.getSwiftSupportRequired().set(false);
		});

		val signTask = taskRegistry.register(namer.determineName(TaskIdentifier.of(variantIdentifier, TaskName.of("sign", "launcherApplicationBundle"))), SignIosApplicationBundleTask.class, task -> {
			task.getUnsignedApplicationBundle().set(createUnitTestApplicationBundleTask.flatMap(CreateIosApplicationBundleTask::getApplicationBundle));
			task.getSignedApplicationBundle().set(layout.getBuildDirectory().file("ios/products/unitTest/" + getTestedComponent().get().getBaseName().get() + ".app"));
			task.getCodeSignatureTool().set(codeSignatureTool);
			task.getCodeSignatureTool().disallowChanges();
		});

		val binaryIdentifierApplicationBundle = BinaryIdentifier.of(BinaryName.of("signedApplicationBundle"), SignedIosApplicationBundleInternal.class, variantIdentifier);
		eventPublisher.publish(new DomainObjectDiscovered<>(binaryIdentifierApplicationBundle));
		val signedApplicationBundle = new SignedIosApplicationBundleInternal(signTask);
		eventPublisher.publish(new DomainObjectCreated<>(binaryIdentifierApplicationBundle, signedApplicationBundle));

		variant.configure(testSuite -> {
			testSuite.getBinaries().configureEach(BundleBinary.class, binary -> {
				((BundleBinaryInternal)binary).getBaseName().set(BaseNameUtils.from(variantIdentifier).getAsCamelCase());
			});
		});

		val bundle = taskRegistry.register(TaskIdentifier.of(TaskName.of("bundle"), variantIdentifier), task -> {
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
}
