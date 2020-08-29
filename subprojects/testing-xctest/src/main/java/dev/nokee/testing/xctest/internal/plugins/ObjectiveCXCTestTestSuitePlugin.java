package dev.nokee.testing.xctest.internal.plugins;

import dev.nokee.language.c.internal.CHeaderSet;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSet;
import dev.nokee.platform.base.DomainObjectElement;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.ios.ObjectiveCIosApplicationExtension;
import dev.nokee.platform.ios.internal.DefaultObjectiveCIosApplicationExtension;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.testing.xctest.internal.DaggerTestingXCTestComponents;
import dev.nokee.testing.xctest.internal.TestingXCTestComponents;
import dev.nokee.testing.xctest.internal.UiTestXCTestTestSuiteComponentImpl;
import dev.nokee.testing.xctest.internal.UnitTestXCTestTestSuiteComponentImpl;
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

			val xcTestComponents = DaggerTestingXCTestComponents.factory().create(project);

			val unitTestComponent = xcTestComponents.unitTestFactory().create(new ComponentIdentifier("unitTest", "iOS unit test XCTest test suite", ProjectIdentifier.of(project)));
			store.add(new DomainObjectElement<UnitTestXCTestTestSuiteComponentImpl>() {
				@Override
				public UnitTestXCTestTestSuiteComponentImpl get() {
					return unitTestComponent;
				}

				@Override
				public Class<UnitTestXCTestTestSuiteComponentImpl> getType() {
					return UnitTestXCTestTestSuiteComponentImpl.class;
				}

				@Override
				public DomainObjectIdentity getIdentity() {
					return DomainObjectIdentity.named("unitTest");
				}
			});
			unitTestComponent.getSourceCollection().add(getObjects().newInstance(ObjectiveCSourceSet.class, "objc").srcDir("src/unitTest/objc"));
			unitTestComponent.getSourceCollection().add(getObjects().newInstance(CHeaderSet.class, "headers").srcDir("src/unitTest/headers"));
			unitTestComponent.getTestedComponent().value(application).disallowChanges();
			unitTestComponent.getGroupId().set(GroupId.of(project::getGroup));
			unitTestComponent.finalizeExtension(project);
			unitTestComponent.getVariantCollection().disallowChanges().realize(); // Force realization, for now

			val uiTestComponent = xcTestComponents.uiTestFactory().create(new ComponentIdentifier("uiTest", "iOS UI test XCTest test suite", ProjectIdentifier.of(project)));
			store.add(new DomainObjectElement<UiTestXCTestTestSuiteComponentImpl>() {
				@Override
				public UiTestXCTestTestSuiteComponentImpl get() {
					return uiTestComponent;
				}

				@Override
				public Class<UiTestXCTestTestSuiteComponentImpl> getType() {
					return UiTestXCTestTestSuiteComponentImpl.class;
				}

				@Override
				public DomainObjectIdentity getIdentity() {
					return DomainObjectIdentity.named("uiTest");
				}
			});
			uiTestComponent.getSourceCollection().add(getObjects().newInstance(ObjectiveCSourceSet.class, "objc").srcDir("src/uiTest/objc"));
			uiTestComponent.getSourceCollection().add(getObjects().newInstance(CHeaderSet.class, "headers").srcDir("src/uiTest/headers"));
			uiTestComponent.getTestedComponent().value(application).disallowChanges();
			uiTestComponent.getGroupId().set(GroupId.of(project::getGroup));
			uiTestComponent.finalizeExtension(project);
			uiTestComponent.getVariantCollection().disallowChanges().realize(); // Force realization, for now
		});
	}
}
