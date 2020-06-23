package dev.nokee.testing.xctest.internal.plugins;

import com.google.common.collect.ImmutableList;
import dev.nokee.internal.Cast;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSet;
import dev.nokee.platform.base.internal.Component;
import dev.nokee.platform.base.internal.ComponentCollection;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.NamingSchemeFactory;
import dev.nokee.platform.ios.ObjectiveCIosApplicationExtension;
import dev.nokee.platform.ios.internal.DefaultObjectiveCIosApplicationExtension;
import dev.nokee.platform.ios.internal.IosApplicationBundleInternal;
import dev.nokee.platform.ios.tasks.internal.CreateIosApplicationBundleTask;
import dev.nokee.platform.ios.tasks.internal.ProcessPropertyListTask;
import dev.nokee.platform.ios.tasks.internal.SignIosApplicationBundleTask;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.testing.xctest.internal.DefaultUiTestXCTestTestSuiteComponent;
import dev.nokee.testing.xctest.internal.DefaultUnitTestXCTestTestSuiteComponent;
import dev.nokee.testing.xctest.tasks.internal.CreateIosXCTestBundleTask;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
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

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getPluginManager().withPlugin("dev.nokee.objective-c-ios-application", appliedPlugin -> {
			BaseNativeComponent<?> application = ((DefaultObjectiveCIosApplicationExtension) project.getExtensions().getByType(ObjectiveCIosApplicationExtension.class)).getComponent();

			ComponentCollection<Component> components = Cast.uncheckedCast("of type erasure", project.getExtensions().getByName("components"));

			val unitTestNames = new NamingSchemeFactory(project.getName()).forMainComponent("unitTest").withComponentDisplayName("iOS unit test XCTest test suite");
			DefaultUnitTestXCTestTestSuiteComponent unitTestComponent = components.register(DefaultUnitTestXCTestTestSuiteComponent.class, unitTestNames).get();
			unitTestComponent.getSourceCollection().add(getObjects().newInstance(ObjectiveCSourceSet.class).srcDir("src/unitTest/objc"));
			unitTestComponent.getTestedComponent().value(application).disallowChanges();
			unitTestComponent.getGroupId().set(GroupId.of(project::getGroup));
			unitTestComponent.finalizeExtension(project);
			unitTestComponent.getVariantCollection().disallowChanges().realize(); // Force realization, for now

			val uiTestNames = new NamingSchemeFactory(project.getName()).forMainComponent("uiTest").withComponentDisplayName("iOS UI test XCTest test suite");
			DefaultUiTestXCTestTestSuiteComponent uiTestComponent = components.register(DefaultUiTestXCTestTestSuiteComponent.class, uiTestNames).get();
			uiTestComponent.getSourceCollection().add(getObjects().newInstance(ObjectiveCSourceSet.class).srcDir("src/uiTest/objc"));
			uiTestComponent.getTestedComponent().value(application).disallowChanges();
			uiTestComponent.getGroupId().set(GroupId.of(project::getGroup));
			uiTestComponent.finalizeExtension(project);
			uiTestComponent.getVariantCollection().disallowChanges().realize(); // Force realization, for now
		});
	}
}
