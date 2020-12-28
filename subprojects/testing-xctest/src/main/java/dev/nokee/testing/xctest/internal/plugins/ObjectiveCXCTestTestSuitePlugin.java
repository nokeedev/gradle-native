package dev.nokee.testing.xctest.internal.plugins;

import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.model.DomainObjectFactory;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactoryRegistry;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.ios.ObjectiveCIosApplicationExtension;
import dev.nokee.platform.ios.internal.DefaultObjectiveCIosApplicationExtension;
import dev.nokee.platform.nativebase.NativeApplicationSources;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.testing.base.TestSuiteContainer;
import dev.nokee.testing.base.internal.plugins.TestingBasePlugin;
import dev.nokee.testing.xctest.internal.DefaultUiTestXCTestTestSuiteComponent;
import dev.nokee.testing.xctest.internal.DefaultUnitTestXCTestTestSuiteComponent;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.util.GUtil;

import javax.inject.Inject;

import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sourceSet;
import static dev.nokee.model.internal.core.ModelActions.register;
import static dev.nokee.model.internal.core.ModelNodes.discover;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.component;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.componentSourcesOf;
import static dev.nokee.platform.objectivec.internal.ObjectiveCSourceSetModelHelpers.configureObjectiveCSourceSetConventionUsingMavenAndGradleCoreNativeLayout;

public class ObjectiveCXCTestTestSuitePlugin implements Plugin<Project> {
	private final ObjectFactory objects;

	@Inject
	public ObjectiveCXCTestTestSuitePlugin(ObjectFactory objectFactory) {
		this.objects = objectFactory;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(TestingBasePlugin.class);
		project.getPluginManager().withPlugin("dev.nokee.objective-c-ios-application", appliedPlugin -> {
			BaseNativeComponent<?> application = ((DefaultObjectiveCIosApplicationExtension) project.getExtensions().getByType(ObjectiveCIosApplicationExtension.class)).getComponent();
			val testSuites = project.getExtensions().getByType(TestSuiteContainer.class);
			val registry = ModelNodes.of(testSuites).get(NodeRegistrationFactoryRegistry.class);
			registry.registerFactory(of(DefaultUnitTestXCTestTestSuiteComponent.class), name -> unitTestXCTestTestSuite(name, project));
			registry.registerFactory(of(DefaultUiTestXCTestTestSuiteComponent.class), name -> uiTestXCTestTestSuite(name, project));

			val unitTestComponentProvider = testSuites.register("unitTest", DefaultUnitTestXCTestTestSuiteComponent.class, component -> {
				component.getTestedComponent().value(application).disallowChanges();
				component.getGroupId().set(GroupId.of(project::getGroup));
				component.getBaseName().set(GUtil.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().get()));
				component.getModuleName().set(GUtil.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().get()));
				component.getProductBundleIdentifier().set(project.getGroup().toString() + "." + GUtil.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().get()));
				component.finalizeExtension(project);
				component.getVariantCollection().realize(); // Force realization, for now
			});
			val unitTestComponent = unitTestComponentProvider.get();

			val uiTestComponentProvider = testSuites.register("uiTest", DefaultUiTestXCTestTestSuiteComponent.class, component -> {
				component.getTestedComponent().value(application).disallowChanges();
				component.getGroupId().set(GroupId.of(project::getGroup));
				component.getBaseName().set(GUtil.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().get()));
				component.getModuleName().set(GUtil.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().get()));
				component.getProductBundleIdentifier().set(project.getGroup().toString() + "." + GUtil.toCamelCase(project.getName()) + StringUtils.capitalize(component.getIdentifier().getName().get()));
				component.finalizeExtension(project);
				component.getVariantCollection().realize(); // Force realization, for now
			});
			val uiTestComponent = uiTestComponentProvider.get();
		});
	}

	public static NodeRegistration<DefaultUnitTestXCTestTestSuiteComponent> unitTestXCTestTestSuite(String name, Project project) {
		return component(name, DefaultUnitTestXCTestTestSuiteComponent.class, () -> {
			val identifier = ComponentIdentifier.of(ComponentName.of(name), DefaultUnitTestXCTestTestSuiteComponent.class, ProjectIdentifier.of(project));
			return newUnitTestFactory(project).create(identifier);
		})
			.action(configureObjectiveCSourceSetConventionUsingMavenAndGradleCoreNativeLayout(ComponentName.of(name)))
			.action(self(discover()).apply(register(
				componentSourcesOf(NativeApplicationSources.class)
					.action(self(discover()).apply(register(sourceSet("objectiveC", ObjectiveCSourceSet.class))))
					.action(self(discover()).apply(register(sourceSet("headers", CHeaderSet.class)))))));
	}

	private static DomainObjectFactory<DefaultUnitTestXCTestTestSuiteComponent> newUnitTestFactory(Project project) {
		return identifier -> {
			return new DefaultUnitTestXCTestTestSuiteComponent((ComponentIdentifier<?>)identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getLayout(), project.getConfigurations(), project.getDependencies(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(VariantRepository.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class), project.getExtensions().getByType(ModelLookup.class));
		};
	}

	public static NodeRegistration<DefaultUiTestXCTestTestSuiteComponent> uiTestXCTestTestSuite(String name, Project project) {
		return component(name, DefaultUiTestXCTestTestSuiteComponent.class, () -> {
			val identifier = ComponentIdentifier.of(ComponentName.of(name), DefaultUiTestXCTestTestSuiteComponent.class, ProjectIdentifier.of(project));
			return newUiTestFactory(project).create(identifier);
		})
			.action(configureObjectiveCSourceSetConventionUsingMavenAndGradleCoreNativeLayout(ComponentName.of(name)))
			.action(self(discover()).apply(register(
				componentSourcesOf(NativeApplicationSources.class)
					.action(self(discover()).apply(register(sourceSet("objectiveC", ObjectiveCSourceSet.class))))
					.action(self(discover()).apply(register(sourceSet("headers", CHeaderSet.class)))))));
	}

	private static DomainObjectFactory<DefaultUiTestXCTestTestSuiteComponent> newUiTestFactory(Project project) {
		return identifier -> {
			return new DefaultUiTestXCTestTestSuiteComponent((ComponentIdentifier<?>)identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getLayout(), project.getConfigurations(), project.getDependencies(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(VariantRepository.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class), project.getExtensions().getByType(ModelLookup.class));
		};
	}
}
