package dev.nokee.ide.xcode.internal;

import dev.nokee.ide.base.internal.IdeBridgeRule;
import dev.nokee.ide.xcode.XcodeIdeBuildConfiguration;
import dev.nokee.ide.xcode.XcodeIdeProject;
import dev.nokee.ide.xcode.XcodeIdeTarget;
import dev.nokee.ide.xcode.internal.tasks.SyncXcodeIdeProduct;
import org.gradle.api.Describable;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

import static dev.nokee.utils.DeferredUtils.realize;
import static dev.nokee.utils.ProjectUtils.getPrefixableProjectPath;

/**
 * Task rule for bridging Xcode IDE with Gradle.
 */
public abstract class XcodeIdeBridge extends IdeBridgeRule<XcodeIdeRequest> {
	public static final String BRIDGE_TASK_NAME = "_xcode__${ACTION}_${PROJECT_NAME}_${TARGET_NAME}_${CONFIGURATION}";
	private final NamedDomainObjectSet<XcodeIdeProject> xcodeProjects;
	private final Project project;

	@Inject
	public XcodeIdeBridge(Describable ide, NamedDomainObjectSet<XcodeIdeProject> xcodeProjects, Project project) {
		super(ide);
		this.xcodeProjects = xcodeProjects;
		this.project = project;
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	protected String getLifecycleTaskNamePrefix() {
		return "_xcode";
	}

	@Override
	public void doHandle(XcodeIdeRequest request) {
		final XcodeIdeTarget target = findXcodeTarget(request);
		SyncXcodeIdeProduct bridgeTask = getTasks().create(request.getTaskName(), SyncXcodeIdeProduct.class);
		bridgeProductBuild(bridgeTask, target, request);
	}

	@Override
	public XcodeIdeRequest newRequest(String taskName) {
		return getObjects().newInstance(XcodeIdeRequest.class, taskName);
	}

	private XcodeIdeTarget findXcodeTarget(XcodeIdeRequest request) {
		String projectName = request.getGradleIdeProjectName();
		realize(xcodeProjects);
		XcodeIdeProject project = xcodeProjects.findByName(projectName);
		if (project == null) {
			throw new GradleException(String.format("Unknown Xcode IDE project '%s', try re-generating the Xcode IDE configuration using '%s:xcode' task.", projectName, getPrefixableProjectPath(this.project)));
		}

		String targetName = request.getTargetName();
		XcodeIdeTarget target = project.getTargets().findByName(targetName);
		if (target == null) {
			throw new GradleException(String.format("Unknown Xcode IDE target '%s', try re-generating the Xcode IDE configuration using '%s:xcode' task.", targetName, getPrefixableProjectPath(this.project)));
		}
		return target;
	}

	private void bridgeProductBuild(SyncXcodeIdeProduct bridgeTask, XcodeIdeTarget target, XcodeIdeRequest request) {
		// Library or executable
		final String configurationName = request.getConfiguration();
		XcodeIdeBuildConfiguration configuration = target.getBuildConfigurations().findByName(configurationName);
		if (configuration == null) {
			throw new GradleException(String.format("Unknown Xcode IDE configuration '%s', try re-generating the Xcode IDE configuration using '%s:xcode' task.", configurationName, getPrefixableProjectPath(this.project)));
		}

		// For XCTest bundle, we have to copy the binary to the BUILT_PRODUCTS_DIR, otherwise Xcode doesn't find the tests.
		// For legacy target, we can work around this by setting the CONFIGURATION_BUILD_DIR to the parent of where Gradle put the built binaries.
		// However, XCTest target are a bit more troublesome and it doesn't seem to play by the same rules as the legacy target.
		// To simplify the configuration, let's always copy the product where Xcode expect it to be.
		// It remove complexity and fragility on the Gradle side.
		final Directory builtProductsPath = request.getBuiltProductsDirectory();
		bridgeTask.getProductLocation().convention(configuration.getProductLocation());
		bridgeTask.getDestinationLocation().convention(builtProductsPath.file(target.getProductReference().get()));
	}
}
