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
import org.gradle.api.Task;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

public final class DefaultUiTestXCTestTestSuiteComponent extends BaseXCTestTestSuiteComponent implements Component {
	private final ObjectFactory objects;
	private final ProviderFactory providers;
	private final TaskRegistry taskRegistry;
	private final ProjectLayout layout;
	private final DomainObjectEventPublisher eventPublisher;

	public DefaultUiTestXCTestTestSuiteComponent(ComponentIdentifier<?> identifier, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ProjectLayout layout, ConfigurationContainer configurations, DependencyHandler dependencyHandler, DomainObjectEventPublisher eventPublisher, VariantViewFactory viewFactory, VariantRepository variantRepository, BinaryViewFactory binaryViewFactory, TaskRegistry taskRegistry, TaskViewFactory taskViewFactory, ModelLookup modelLookup) {
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

		// XCTest UI Testing
		val processUiTestPropertyListTask = taskRegistry.register("processUiTestPropertyList", ProcessPropertyListTask.class, task -> {
			task.getIdentifier().set(providers.provider(() -> getGroupId().get().get().get() + "." + moduleName));
			task.getModule().set(moduleName);
			task.getSources().from("src/uiTest/resources/Info.plist");
			task.getOutputFile().set(layout.getBuildDirectory().file("ios/uiTest/Info.plist"));
		});

		TaskProvider<CreateIosXCTestBundleTask> createUiTestXCTestBundle = taskRegistry.register("createUiTestXCTestBundle", CreateIosXCTestBundleTask.class, task -> {
			task.getXCTestBundle().set(layout.getBuildDirectory().file("ios/products/uiTest/" + moduleName + "-Runner-unsigned.xctest"));
			task.getSources().from(processUiTestPropertyListTask.flatMap(it -> it.getOutputFile()));
			task.getSources().from(variant.flatMap(testSuite -> testSuite.getBinaries().withType(BundleBinary.class).getElements().map(binaries -> binaries.stream().map(binary -> binary.getLinkTask().get().getLinkedFile()).collect(Collectors.toList()))));
		});

		Provider<CommandLineTool> codeSignatureTool = providers.provider(() -> CommandLineTool.of(new File("/usr/bin/codesign")));
		TaskProvider<SignIosApplicationBundleTask> signUiTestXCTestBundle = taskRegistry.register("signUiTestXCTestBundle", SignIosApplicationBundleTask.class, task -> {
			task.getUnsignedApplicationBundle().set(createUiTestXCTestBundle.flatMap(CreateIosXCTestBundleTask::getXCTestBundle));
			task.getSignedApplicationBundle().set(layout.getBuildDirectory().file("ios/products/uiTest/" + moduleName + ".xctest"));
			task.getCodeSignatureTool().set(codeSignatureTool);
			task.getCodeSignatureTool().disallowChanges();
		});

		TaskProvider<CreateIosApplicationBundleTask> createUiTestApplicationBundleTask = taskRegistry.register("createUiTestLauncherApplicationBundle", CreateIosApplicationBundleTask.class, task -> {
			task.getApplicationBundle().set(layout.getBuildDirectory().file("ios/products/uiTest/" + moduleName + "-Runner-unsigned.app"));
			task.getSources().from(getXCTRunner());
			task.getPlugIns().from(signUiTestXCTestBundle.flatMap(SignIosApplicationBundleTask::getSignedApplicationBundle));
			task.getFrameworks().from(getXCTestFrameworks());
			task.getSwiftSupportRequired().set(false);
		});

		val binaryIdentifierApplicationBundle = BinaryIdentifier.of(BinaryName.of("launcherApplicationBundle"), IosApplicationBundleInternal.class, variant.getIdentifier());
		eventPublisher.publish(new DomainObjectDiscovered<>(binaryIdentifierApplicationBundle));
		val launcherApplicationBundle = new IosApplicationBundleInternal(createUiTestApplicationBundleTask);
		eventPublisher.publish(new DomainObjectCreated<>(binaryIdentifierApplicationBundle, launcherApplicationBundle));

		val signTask = taskRegistry.register("signUiTestLauncherApplicationBundle", SignIosApplicationBundleTask.class, task -> {
			task.getUnsignedApplicationBundle().set(createUiTestApplicationBundleTask.flatMap(CreateIosApplicationBundleTask::getApplicationBundle));
			task.getSignedApplicationBundle().set(layout.getBuildDirectory().file("ios/products/uiTest/" + moduleName + "-Runner.app"));
			task.getCodeSignatureTool().set(codeSignatureTool);
			task.getCodeSignatureTool().disallowChanges();
		});

		val binaryIdentifierSignedApplicationBundle = BinaryIdentifier.of(BinaryName.of("signedLauncherApplicationBundle"), SignedIosApplicationBundleInternal.class, variant.getIdentifier());
		eventPublisher.publish(new DomainObjectDiscovered<>(binaryIdentifierSignedApplicationBundle));
		val signedLauncherApplicationBundle = new SignedIosApplicationBundleInternal(signTask);
		eventPublisher.publish(new DomainObjectCreated<>(binaryIdentifierSignedApplicationBundle, signedLauncherApplicationBundle));
		variant.configure(it -> it.getDevelopmentBinary().set(signedLauncherApplicationBundle));

		variant.configure(testSuite -> {
			testSuite.getBinaries().configureEach(BundleBinary.class, binary -> {
				((BundleBinaryInternal)binary).getBaseName().set(BaseNameUtils.from(variant.getIdentifier()).getAsCamelCase());
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
