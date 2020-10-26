package dev.nokee.ide.xcode.internal.plugins;

import dev.nokee.ide.base.internal.*;
import dev.nokee.ide.base.internal.plugins.AbstractIdePlugin;
import dev.nokee.ide.xcode.XcodeIdeProject;
import dev.nokee.ide.xcode.XcodeIdeProjectExtension;
import dev.nokee.ide.xcode.internal.*;
import dev.nokee.ide.xcode.internal.rules.CreateNativeComponentXcodeIdeProject;
import dev.nokee.ide.xcode.internal.services.XcodeIdeGidGeneratorService;
import dev.nokee.ide.xcode.internal.tasks.GenerateXcodeIdeWorkspaceTask;
import dev.nokee.ide.xcode.internal.tasks.SyncXcodeIdeProduct;
import dev.nokee.language.base.internal.LanguageSourceSetRepository;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.BaseComponent;
import dev.nokee.platform.base.internal.components.ComponentConfigurer;
import dev.nokee.platform.base.internal.components.KnownComponent;
import dev.nokee.platform.base.internal.components.KnownComponentFactory;
import dev.nokee.platform.base.internal.plugins.ComponentBasePlugin;
import dev.nokee.platform.ios.tasks.internal.CreateIosApplicationBundleTask;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.Rule;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.Provider;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.internal.Actions;
import org.gradle.plugins.ide.internal.IdeProjectMetadata;
import org.gradle.util.GUtil;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import static dev.nokee.utils.ProjectUtils.getPrefixableProjectPath;

public abstract class XcodeIdePlugin extends AbstractIdePlugin<XcodeIdeProject> {
	public static final String XCODE_EXTENSION_NAME = "xcode";

	@Override
	public void doProjectApply(IdeProjectExtension<XcodeIdeProject> extension) {
		DefaultXcodeIdeProjectExtension projectExtension = (DefaultXcodeIdeProjectExtension) extension;

		Provider<XcodeIdeGidGeneratorService> xcodeIdeGidGeneratorService = getProject().getGradle().getSharedServices().registerIfAbsent("xcodeIdeGidGeneratorService", XcodeIdeGidGeneratorService.class, Actions.doNothing());
		projectExtension.getProjects().withType(DefaultXcodeIdeProject.class).configureEach(xcodeProject -> {
			xcodeProject.getSources().from(getBuildFiles());
			xcodeProject.getGeneratorTask().configure( task -> {
				FileSystemLocation projectLocation = getLayout().getProjectDirectory().dir(xcodeProject.getName() + ".xcodeproj");
				task.getProjectLocation().convention(projectLocation);
				task.usesService(xcodeIdeGidGeneratorService);
				task.getGidGenerator().set(xcodeIdeGidGeneratorService);
				task.getGradleCommand().set(toGradleCommand(getProject().getGradle()));
				task.getBridgeTaskPath().set(getBridgeTaskPath());
				task.getAdditionalGradleArguments().set(getAdditionalBuildArguments());
			});
		});

		getProject().getTasks().addRule(getObjects().newInstance(XcodeIdeBridge.class, this, projectExtension.getProjects(), getProject()));
		getProject().getPlugins().withType(ComponentBasePlugin.class, mapComponentToXcodeIdeProjects(extension));
		getProject().getPluginManager().withPlugin("dev.nokee.objective-c-xctest-test-suite", appliedPlugin -> {
			String moduleName = GUtil.toCamelCase(getProject().getName());

			// The Xcode IDE model will sync the `.xctest` product but we need the `-Runner.app`.
			// We can't just sync the `-Runner.app` as Xcode will complain about a missing `.xctest`.
			// Also the file under the `Products` group depends what product is synced.
			// It's better to sync both files over to the BUILT_PRODUCTS_DIR.
			getTasks().register("syncUiTestRunner", SyncXcodeIdeProduct.class, task -> {
				task.getProductLocation().set(getTasks().named("createUiTestLauncherApplicationBundle", CreateIosApplicationBundleTask.class).flatMap(CreateIosApplicationBundleTask::getApplicationBundle));

				// TODO: To improve the situation, we should provide XcodeIdePropertyAdapter and/or XcodeIdeRequest through this task.
				//  If we clean up the situation by exposing the lifecycle/sync task through the model, we could avoid exposing the information on the task
				//  However, it would be convenient for the users that need to hack things together.
				//  It should also be possible to achieve similar integration in term of complexity only via public APIs.
				//  It will need to be evaluated in the bigger context.
				val properties = getObjects().newInstance(XcodeIdePropertyAdapter.class);
				task.getDestinationLocation().set(getObjects().directoryProperty().fileProvider(properties.getBuiltProductsDir().map(File::new)).file(moduleName + "UiTest-Runner.app"));
			});

			getTasks().withType(SyncXcodeIdeProduct.class).configureEach(task -> {
				// Complete wiring for syncing the `-Runner.app` for UI testing.
				if (task.getName().contains(moduleName + "UiTest")) {
					task.dependsOn("syncUiTestRunner");

					// We also depends on the tested application lifecycle task here (very poorly), as it's referenced via the TEST_TARGET_NAME build setting.
					task.dependsOn(task.getName().replace("UiTest", ""));
				}

//				// MAYBE???
//				if (task.getName().contains(moduleName + "UnitTest")) {
//					task.getDestinationLocation().set(getObjects().directoryProperty().fileValue(new File(new XcodeIdePropertyAdapter(project).getBuiltProductsDir()/*.replace("/Default-", "/__NokeeTestRunner_Default-")*/)).file(moduleName + "UnitTest.xctest"));
//				}
				// TODO: Environment variable should be part of the task inputs for instant execution.
				//  We are also over reaching quite a bit to get the list of files required, etc.
				//  If we add the lifecycle tasks to the XcodeIde* model, we could clean up that over reaching.
				if (task.getName().contains(moduleName + "UnitTest") || task.getName().contains(moduleName + "UiTest")) {
					task.doLast(new Action<Task>() {
						@Override
						public void execute(Task task) {
							XcodeIdeRequest request = getObjects().newInstance(XcodeIdeRequest.class, task.getName());
							FileCollection sources = getProject().getExtensions().getByType(XcodeIdeProjectExtension.class).getProjects().getByName(request.getProjectName()).getTargets().getByName(request.getTargetName()).getSources();
							for (String arch : StringUtils.split(System.getenv("ARCHS"), ' ')) {
								String objectFileDir = System.getenv("OBJECT_FILE_DIR");
								String productName = System.getenv("PRODUCT_NAME");
								File dependencyInfo = new File(objectFileDir + "-normal/" + arch, productName + "_dependency_info.dat");
								dependencyInfo.getParentFile().mkdirs();
								try {
									FileUtils.writeByteArrayToFile(dependencyInfo, new byte[] {0, 0x31, 0});
									for (File file : sources) {
										new File(objectFileDir + "-normal/" + arch, FilenameUtils.removeExtension(file.getName()) + ".d").createNewFile();
									}
								} catch (IOException e) {
									throw new UncheckedIOException(e);
								}
							}
						}
					});
				}
			});
		});
	}

	private Action<ComponentBasePlugin> mapComponentToXcodeIdeProjects(IdeProjectExtension<XcodeIdeProject> extension) {
		return new Action<ComponentBasePlugin>() {
			private KnownComponentFactory knownComponentFactory;

			private KnownComponentFactory getKnownComponentFactory() {
				if (knownComponentFactory == null) {
					knownComponentFactory = getProject().getExtensions().getByType(KnownComponentFactory.class);
				}
				return knownComponentFactory;
			}

			@Override
			public void execute(ComponentBasePlugin appliedPlugin) {
				val componentConfigurer = getProject().getExtensions().getByType(ComponentConfigurer.class);
				componentConfigurer.whenElementKnown(ProjectIdentifier.of(getProject()), getComponentImplementationType(), asKnownComponent(new CreateNativeComponentXcodeIdeProject(extension, getProject().getProviders(), getProject().getObjects(), getProject().getExtensions().getByType(LanguageSourceSetRepository.class), getProject().getLayout(), getProject().getTasks(), ProjectIdentifier.of(getProject()))));
			}

			private <T extends Component> Action<? super TypeAwareDomainObjectIdentifier<T>> asKnownComponent(Action<? super KnownComponent<T>> action) {
				return identifier -> action.execute(getKnownComponentFactory().create(identifier));
			}

			private Class<BaseComponent<?>> getComponentImplementationType() {
				return new TypeOf<BaseComponent<?>>() {}.getConcreteClass();
			}
		};
	}

	@Override
	public void doWorkspaceApply(IdeWorkspaceExtension<XcodeIdeProject> extension) {
		DefaultXcodeIdeWorkspaceExtension workspaceExtension = (DefaultXcodeIdeWorkspaceExtension) extension;

		workspaceExtension.getWorkspace().getGeneratorTask().configure(task -> {
			task.getWorkspaceLocation().set(getLayout().getProjectDirectory().dir(getProject().getName() + ".xcworkspace"));
			task.getProjectReferences().set(workspaceExtension.getWorkspace().getProjects());
			task.getDerivedDataLocation().set(".gradle/XcodeDerivedData");
		});

		getCleanTask().configure(task -> {
			task.delete(workspaceExtension.getWorkspace().getGeneratorTask().flatMap(GenerateXcodeIdeWorkspaceTask::getDerivedDataLocation));
		});
	}

	@Override
	protected String getExtensionName() {
		return XCODE_EXTENSION_NAME;
	}

	@Override
	protected IdeProjectMetadata newIdeProjectMetadata(Provider<IdeProjectInternal> ideProject) {
		return new DefaultXcodeIdeProjectReference(ideProject.map(DefaultXcodeIdeProject.class::cast));
	}

	@Override
	protected Class<? extends BaseIdeProjectReference> getIdeProjectReferenceType() {
		return DefaultXcodeIdeProjectReference.class;
	}

	@Override
	protected IdeProjectMetadata newIdeCleanMetadata(Provider<? extends Task> cleanTask) {
		return new XcodeIdeCleanMetadata(cleanTask);
	}

	@Override
	protected Class<? extends BaseIdeCleanMetadata> getIdeCleanMetadataType() {
		return XcodeIdeCleanMetadata.class;
	}

	@Override
	protected IdeWorkspaceExtension<XcodeIdeProject> newIdeWorkspaceExtension() {
		return getObjects().newInstance(DefaultXcodeIdeWorkspaceExtension.class);
	}

	@Override
	protected IdeProjectExtension<XcodeIdeProject> newIdeProjectExtension() {
		return getObjects().newInstance(DefaultXcodeIdeProjectExtension.class);
	}

	@Inject
	protected abstract ProjectLayout getLayout();

	/**
	 * Returns the task name format to uses when delegating to Gradle.
	 * When Gradle is invoked with tasks following the name format, it is delegated to {@link XcodeIdeBridge} via {@link TaskContainer#addRule(Rule)}.
	 *
	 * @return a fully qualified task path format for the {@literal PBXLegacyTarget} target type to realize using the build settings from within Xcode IDE.
	 */
	private String getBridgeTaskPath() {
		return getPrefixableProjectPath(getProject()) + ":" + XcodeIdeBridge.BRIDGE_TASK_NAME;
	}
}
