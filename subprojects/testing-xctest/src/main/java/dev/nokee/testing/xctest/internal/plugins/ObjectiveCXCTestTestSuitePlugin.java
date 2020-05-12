package dev.nokee.testing.xctest.internal.plugins;

import com.google.common.collect.ImmutableList;
import dev.nokee.platform.ios.tasks.internal.CreateIosApplicationBundleTask;
import dev.nokee.platform.ios.tasks.internal.ProcessPropertyListTask;
import dev.nokee.platform.ios.tasks.internal.SignIosApplicationBundleTask;
import dev.nokee.testing.xctest.tasks.internal.CreateIosXCTestBundleTask;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.util.GUtil;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public abstract class ObjectiveCXCTestTestSuitePlugin implements Plugin<Project> {

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ProjectLayout getLayout();

	@Inject
	protected abstract ProviderFactory getProviders();

	@Override
	public void apply(Project project) {
		project.getPluginManager().withPlugin("dev.nokee.objective-c-ios-application", appliedPlugin -> {
			project.getPluginManager().apply(XCTestRules.class);

			String moduleName = GUtil.toCamelCase(project.getName());

			// XCTest Unit Testing
			TaskProvider<ProcessPropertyListTask> processUnitTestPropertyListTask = getTasks().register("processUnitTestPropertyList", ProcessPropertyListTask.class, task -> {
				task.getIdentifier().set(project.provider(() -> project.getGroup().toString() + "." + moduleName + "UnitTest"));
				task.getModule().set(moduleName + "UnitTest");
				task.getSources().from("src/unitTest/resources/Info.plist");
				task.getOutputFile().set(getLayout().getBuildDirectory().file("ios/unitTest/Info.plist"));
			});

			TaskProvider<CreateIosXCTestBundleTask> createUnitTestXCTestBundle = getTasks().register("createUnitTestXCTestBundle", CreateIosXCTestBundleTask.class, task -> {
				task.getXCTestBundle().set(getLayout().getBuildDirectory().file("ios/products/unitTest/" + moduleName + "UnitTest-unsigned.xctest"));
				task.getSources().from(processUnitTestPropertyListTask.flatMap(it -> it.getOutputFile()));
				// Linked files are configured in XCTestRules
			});

			TaskProvider<SignIosApplicationBundleTask> signUnitTestXCTestBundle = getTasks().register("signUnitTestXCTestBundle", SignIosApplicationBundleTask.class, task -> {
				task.getUnsignedApplicationBundle().set(createUnitTestXCTestBundle.flatMap(CreateIosXCTestBundleTask::getXCTestBundle));
				task.getSignedApplicationBundle().set(getLayout().getBuildDirectory().file("ios/products/unitTest/" + moduleName + "UnitTest.xctest"));
			});

			TaskProvider<CreateIosApplicationBundleTask> createUnitTestApplicationBundleTask = getTasks().register("createUnitTestLauncherApplicationBundle", CreateIosApplicationBundleTask.class, task -> {
				TaskProvider<CreateIosApplicationBundleTask> createApplicationBundleTask = getTasks().named("createApplicationBundle", CreateIosApplicationBundleTask.class);
				task.getApplicationBundle().set(getLayout().getBuildDirectory().file("ios/products/unitTest/" + moduleName + "-unsigned.app"));
				task.getSources().from(createApplicationBundleTask.map(CreateIosApplicationBundleTask::getSources));
				task.getPlugIns().from(signUnitTestXCTestBundle.flatMap(SignIosApplicationBundleTask::getSignedApplicationBundle));
				task.getFrameworks().from(getXCTestBundleInjectDynamicLibrary());
				task.getFrameworks().from(getXCTestFrameworks());
			});

			getTasks().register("signUnitTestLauncherApplicationBundle", SignIosApplicationBundleTask.class, task -> {
				task.getUnsignedApplicationBundle().set(createUnitTestApplicationBundleTask.flatMap(CreateIosApplicationBundleTask::getApplicationBundle));
				task.getSignedApplicationBundle().set(getLayout().getBuildDirectory().file("ios/products/unitTest/" + moduleName + ".app"));
			});


			// XCTest UI Testing
			TaskProvider<ProcessPropertyListTask> processUiTestPropertyListTask = getTasks().register("processUiTestPropertyList", ProcessPropertyListTask.class, task -> {
				task.getIdentifier().set(project.provider(() -> project.getGroup().toString() + "." + moduleName + "UiTest"));
				task.getModule().set(moduleName + "UiTest");
				task.getSources().from("src/uiTest/resources/Info.plist");
				task.getOutputFile().set(getLayout().getBuildDirectory().file("ios/uiTest/Info.plist"));
			});

			TaskProvider<CreateIosXCTestBundleTask> createUiTestXCTestBundle = getTasks().register("createUiTestXCTestBundle", CreateIosXCTestBundleTask.class, task -> {
				task.getXCTestBundle().set(getLayout().getBuildDirectory().file("ios/products/uiTest/" + moduleName + "UiTest-Runner-unsigned.xctest"));
				task.getSources().from(processUiTestPropertyListTask.flatMap(it -> it.getOutputFile()));
				// Linked files are configured in XCTestRules
			});

			TaskProvider<SignIosApplicationBundleTask> signUiTestXCTestBundle = getTasks().register("signUiTestXCTestBundle", SignIosApplicationBundleTask.class, task -> {
				task.getUnsignedApplicationBundle().set(createUiTestXCTestBundle.flatMap(CreateIosXCTestBundleTask::getXCTestBundle));
				task.getSignedApplicationBundle().set(getLayout().getBuildDirectory().file("ios/products/uiTest/" + moduleName + "UiTest.xctest"));
			});

			TaskProvider<CreateIosApplicationBundleTask> createUiTestApplicationBundleTask = getTasks().register("createUiTestLauncherApplicationBundle", CreateIosApplicationBundleTask.class, task -> {
				task.getApplicationBundle().set(getLayout().getBuildDirectory().file("ios/products/uiTest/" + moduleName + "UiTest-Runner-unsigned.app"));
				task.getSources().from(getXCTRunner());
				task.getPlugIns().from(signUiTestXCTestBundle.flatMap(SignIosApplicationBundleTask::getSignedApplicationBundle));
				task.getFrameworks().from(getXCTestFrameworks());
			});

			getTasks().register("signUiTestLauncherApplicationBundle", SignIosApplicationBundleTask.class, task -> {
				task.getUnsignedApplicationBundle().set(createUiTestApplicationBundleTask.flatMap(CreateIosApplicationBundleTask::getApplicationBundle));
				task.getSignedApplicationBundle().set(getLayout().getBuildDirectory().file("ios/products/uiTest/" + moduleName + "UiTest-Runner.app"));
			});
		});
	}

	private Provider<File> getXCTestBundleInjectDynamicLibrary() {
		return getProviders().provider(() -> new File(getSdkPlatformPath(), "Developer/usr/lib/libXCTestBundleInject.dylib"));
	}

	private Provider<List<File>> getXCTestFrameworks() {
		return getProviders().provider(() -> {
			return ImmutableList.<File>builder()
				.add(new File(getSdkPlatformPath(), "Developer/Library/PrivateFrameworks/XCTAutomationSupport.framework"))
				.add(new File(getSdkPlatformPath(), "Developer/usr/lib/libXCTestSwiftSupport.dylib"))
				.add(new File(getSdkPlatformPath(), "Developer/Library/Frameworks/XCTest.framework"))
				.build();
		});
	}

	private Provider<File> getXCTRunner() {
		return getProviders().provider(() -> {
			return new File(getSdkPlatformPath(), "Developer/Library/Xcode/Agents/XCTRunner.app/XCTRunner");
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
