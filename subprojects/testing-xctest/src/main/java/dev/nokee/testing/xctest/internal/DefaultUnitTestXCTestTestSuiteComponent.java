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
import dev.nokee.model.internal.DomainObjectCreated;
import dev.nokee.model.internal.DomainObjectDiscovered;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.BaseNameUtils;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.BinaryName;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.base.internal.variants.KnownVariant;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.ios.internal.IosApplicationBundleInternal;
import dev.nokee.platform.ios.internal.SignedIosApplicationBundleInternal;
import dev.nokee.platform.ios.tasks.internal.CreateIosApplicationBundleTask;
import dev.nokee.platform.ios.tasks.internal.ProcessPropertyListTask;
import dev.nokee.platform.ios.tasks.internal.SignIosApplicationBundleTask;
import dev.nokee.platform.nativebase.BundleBinary;
import dev.nokee.platform.nativebase.internal.BundleBinaryInternal;
import dev.nokee.testing.xctest.tasks.internal.CreateIosXCTestBundleTask;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
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

public final class DefaultUnitTestXCTestTestSuiteComponent extends BaseXCTestTestSuiteComponent implements Component {
	private final ObjectFactory objects;
	private final ProviderFactory providers;
	private final TaskRegistry taskRegistry;
	private final ProjectLayout layout;
	private final DomainObjectEventPublisher eventPublisher;

	public DefaultUnitTestXCTestTestSuiteComponent(ComponentIdentifier<?> identifier, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ProjectLayout layout, ConfigurationContainer configurations, DependencyHandler dependencyHandler, DomainObjectEventPublisher eventPublisher, VariantViewFactory viewFactory, VariantRepository variantRepository, BinaryViewFactory binaryViewFactory, TaskRegistry taskRegistry, TaskViewFactory taskViewFactory, ModelLookup modelLookup) {
		super(identifier, objects, providers, tasks, layout, configurations, dependencyHandler, eventPublisher, viewFactory, variantRepository, binaryViewFactory, taskRegistry, taskViewFactory, modelLookup);
		this.objects = objects;
		this.providers = providers;
		this.taskRegistry = taskRegistry;
		this.layout = layout;
		this.eventPublisher = eventPublisher;
	}

	@Override
	protected void onEachVariant(KnownVariant<DefaultXCTestTestSuiteVariant> variant) {
		super.onEachVariant(variant);
		val variantIdentifier = variant.getIdentifier();

		String moduleName = BaseNameUtils.from(variant.getIdentifier()).getAsCamelCase();

		// XCTest Unit Testing
		val processUnitTestPropertyListTask = taskRegistry.register("processUnitTestPropertyList", ProcessPropertyListTask.class, task -> {
			task.getIdentifier().set(providers.provider(() -> getGroupId().get().get().get() + "." + moduleName));
			task.getModule().set(moduleName);
			task.getSources().from("src/unitTest/resources/Info.plist");
			task.getOutputFile().set(layout.getBuildDirectory().file("ios/unitTest/Info.plist"));
		});

		val createUnitTestXCTestBundle = taskRegistry.register("createUnitTestXCTestBundle", CreateIosXCTestBundleTask.class, task -> {
			task.getXCTestBundle().set(layout.getBuildDirectory().file("ios/products/unitTest/" + moduleName + "-unsigned.xctest"));
			task.getSources().from(processUnitTestPropertyListTask.flatMap(it -> it.getOutputFile()));
			task.getSources().from(variant.flatMap(testSuite -> testSuite.getBinaries().withType(BundleBinary.class).getElements().map(binaries -> binaries.stream().map(binary -> binary.getLinkTask().get().getLinkedFile()).collect(Collectors.toList()))));
		});
		Provider<CommandLineTool> codeSignatureTool = providers.provider(() -> CommandLineTool.of(new File("/usr/bin/codesign")));
		val signUnitTestXCTestBundle = taskRegistry.register("signUnitTestXCTestBundle", SignIosApplicationBundleTask.class, task -> {
			task.getUnsignedApplicationBundle().set(createUnitTestXCTestBundle.flatMap(CreateIosXCTestBundleTask::getXCTestBundle));
			task.getSignedApplicationBundle().set(layout.getBuildDirectory().file("ios/products/unitTest/" + moduleName + ".xctest"));
			task.getCodeSignatureTool().set(codeSignatureTool);
			task.getCodeSignatureTool().disallowChanges();
		});

		val binaryIdentifierXCTestBundle = BinaryIdentifier.of(BinaryName.of("unitTestXCTestBundle"), IosXCTestBundle.class, variant.getIdentifier());
		eventPublisher.publish(new DomainObjectDiscovered<>(binaryIdentifierXCTestBundle));
		val xcTestBundle = new IosXCTestBundle(createUnitTestXCTestBundle);
		eventPublisher.publish(new DomainObjectCreated<>(binaryIdentifierXCTestBundle, xcTestBundle));
		// We could use signed bundle as development binary but right now it's only used in Xcode which Xcode will perform the signing so no need to provide a signed bundle
		variant.configure(it -> it.getDevelopmentBinary().set(xcTestBundle));

		val createUnitTestApplicationBundleTask = taskRegistry.register("createUnitTestLauncherApplicationBundle", CreateIosApplicationBundleTask.class, task -> {
			task.getApplicationBundle().set(layout.getBuildDirectory().file("ios/products/unitTest/" + getTestedComponent().get().getBaseName().get() + "-unsigned.app"));
			task.getSources().from(getTestedComponent().flatMap(c -> c.getVariants().getElements().map(it -> it.iterator().next().getBinaries().withType(IosApplicationBundleInternal.class).get().iterator().next().getBundleTask().map(t -> t.getSources()))));
			task.getPlugIns().from(signUnitTestXCTestBundle.flatMap(SignIosApplicationBundleTask::getSignedApplicationBundle));
			task.getFrameworks().from(getXCTestBundleInjectDynamicLibrary());
			task.getFrameworks().from(getXCTestFrameworks());
			task.getSwiftSupportRequired().set(false);
		});

		val signTask = taskRegistry.register("signUnitTestLauncherApplicationBundle", SignIosApplicationBundleTask.class, task -> {
			task.getUnsignedApplicationBundle().set(createUnitTestApplicationBundleTask.flatMap(CreateIosApplicationBundleTask::getApplicationBundle));
			task.getSignedApplicationBundle().set(layout.getBuildDirectory().file("ios/products/unitTest/" + getTestedComponent().get().getBaseName().get() + ".app"));
			task.getCodeSignatureTool().set(codeSignatureTool);
			task.getCodeSignatureTool().disallowChanges();
		});

		val binaryIdentifierApplicationBundle = BinaryIdentifier.of(BinaryName.of("signedApplicationBundle"), SignedIosApplicationBundleInternal.class, variant.getIdentifier());
		eventPublisher.publish(new DomainObjectDiscovered<>(binaryIdentifierApplicationBundle));
		val signedApplicationBundle = new SignedIosApplicationBundleInternal(signTask);
		eventPublisher.publish(new DomainObjectCreated<>(binaryIdentifierApplicationBundle, signedApplicationBundle));

		variant.configure(testSuite -> {
			testSuite.getBinaries().configureEach(BundleBinary.class, binary -> {
				((BundleBinaryInternal)binary).getBaseName().set(BaseNameUtils.from(variant.getIdentifier()).getAsCamelCase());
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
