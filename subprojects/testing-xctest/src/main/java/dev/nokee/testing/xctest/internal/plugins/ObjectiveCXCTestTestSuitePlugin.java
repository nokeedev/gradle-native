package dev.nokee.testing.xctest.internal.plugins;

import dev.nokee.language.c.internal.CHeaderSet;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSet;
import dev.nokee.platform.base.internal.DomainObjectStore;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.NamingSchemeFactory;
import dev.nokee.platform.ios.ObjectiveCIosApplicationExtension;
import dev.nokee.platform.ios.internal.DefaultObjectiveCIosApplicationExtension;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.testing.xctest.internal.DefaultUiTestXCTestTestSuiteComponent;
import dev.nokee.testing.xctest.internal.DefaultUnitTestXCTestTestSuiteComponent;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;

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
			val store = project.getExtensions().getByType(DomainObjectStore.class);

			val unitTestComponent = store.register(DefaultUnitTestXCTestTestSuiteComponent.newUnitTest(getObjects(), new NamingSchemeFactory(project.getName())));
			unitTestComponent.configure(component -> {
				component.getSourceCollection().add(getObjects().newInstance(ObjectiveCSourceSet.class, "objc").srcDir("src/unitTest/objc"));
				component.getSourceCollection().add(getObjects().newInstance(CHeaderSet.class, "headers").srcDir("src/unitTest/headers"));
				component.getTestedComponent().value(application).disallowChanges();
				component.getGroupId().set(GroupId.of(project::getGroup));
				component.finalizeExtension(project);
				component.getVariantCollection().disallowChanges().realize(); // Force realization, for now
			});
			unitTestComponent.get();

			val uiTestComponent = store.register(DefaultUiTestXCTestTestSuiteComponent.newUiTest(getObjects(), new NamingSchemeFactory(project.getName())));
			uiTestComponent.configure(component -> {
				component.getSourceCollection().add(getObjects().newInstance(ObjectiveCSourceSet.class, "objc").srcDir("src/uiTest/objc"));
				component.getSourceCollection().add(getObjects().newInstance(CHeaderSet.class, "headers").srcDir("src/uiTest/headers"));
				component.getTestedComponent().value(application).disallowChanges();
				component.getGroupId().set(GroupId.of(project::getGroup));
				component.finalizeExtension(project);
				component.getVariantCollection().disallowChanges().realize(); // Force realization, for now
			});
			uiTestComponent.get();
		});
	}
}
