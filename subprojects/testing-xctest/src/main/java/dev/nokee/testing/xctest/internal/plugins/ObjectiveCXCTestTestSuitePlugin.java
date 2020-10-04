package dev.nokee.testing.xctest.internal.plugins;

import dev.nokee.language.c.internal.CHeaderSet;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSet;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.ios.ObjectiveCIosApplicationExtension;
import dev.nokee.platform.ios.internal.DefaultObjectiveCIosApplicationExtension;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.testing.xctest.internal.DefaultUiTestXCTestTestSuiteComponent;
import dev.nokee.testing.xctest.internal.DefaultUnitTestXCTestTestSuiteComponent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;

import static dev.nokee.testing.xctest.internal.DefaultUiTestXCTestTestSuiteComponent.newUiTestFactory;
import static dev.nokee.testing.xctest.internal.DefaultUnitTestXCTestTestSuiteComponent.newUnitTestFactory;

public class ObjectiveCXCTestTestSuitePlugin implements Plugin<Project> {
	@Getter(AccessLevel.PROTECTED) private final TaskContainer tasks;
	@Getter(AccessLevel.PROTECTED) private final ProjectLayout layout;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public ObjectiveCXCTestTestSuitePlugin(TaskContainer tasks, ProjectLayout layout, ProviderFactory providers, ObjectFactory objects) {
		this.tasks = tasks;
		this.layout = layout;
		this.providers = providers;
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().withPlugin("dev.nokee.objective-c-ios-application", appliedPlugin -> {
			BaseNativeComponent<?> application = ((DefaultObjectiveCIosApplicationExtension) project.getExtensions().getByType(ObjectiveCIosApplicationExtension.class)).getComponent();
			val store = project.getExtensions().getByType(DomainObjectStore.class);

			val unitTestIdentifier = ComponentIdentifier.of(ComponentName.of("unitTest"), DefaultUnitTestXCTestTestSuiteComponent.class, ProjectIdentifier.of(project));
			val unitTestComponent = store.register(unitTestIdentifier, DefaultUnitTestXCTestTestSuiteComponent.class, newUnitTestFactory(getObjects(), project));
			unitTestComponent.configure(component -> {
				component.getSourceCollection().add(getObjects().newInstance(ObjectiveCSourceSet.class, "objc").srcDir("src/unitTest/objc"));
				component.getSourceCollection().add(getObjects().newInstance(CHeaderSet.class, "headers").srcDir("src/unitTest/headers"));
				component.getTestedComponent().value(application).disallowChanges();
				component.getGroupId().set(GroupId.of(project::getGroup));
				component.finalizeExtension(project);
				component.getVariantCollection().realize(); // Force realization, for now
			});
			unitTestComponent.get();

			val uiTestIdentifier = ComponentIdentifier.of(ComponentName.of("uiTest"), DefaultUnitTestXCTestTestSuiteComponent.class, ProjectIdentifier.of(project));
			val uiTestComponent = store.register(uiTestIdentifier, DefaultUiTestXCTestTestSuiteComponent.class, newUiTestFactory(getObjects(), project));
			uiTestComponent.configure(component -> {
				component.getSourceCollection().add(getObjects().newInstance(ObjectiveCSourceSet.class, "objc").srcDir("src/uiTest/objc"));
				component.getSourceCollection().add(getObjects().newInstance(CHeaderSet.class, "headers").srcDir("src/uiTest/headers"));
				component.getTestedComponent().value(application).disallowChanges();
				component.getGroupId().set(GroupId.of(project::getGroup));
				component.finalizeExtension(project);
				component.getVariantCollection().realize(); // Force realization, for now
			});
			uiTestComponent.get();
		});
	}
}
