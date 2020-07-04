package dev.nokee.ide.visualstudio.internal;

import dev.nokee.ide.base.internal.IdeBridgeRule;
import dev.nokee.ide.visualstudio.VisualStudioIdeConfiguration;
import dev.nokee.ide.visualstudio.VisualStudioIdePlatform;
import dev.nokee.ide.visualstudio.VisualStudioIdeProject;
import dev.nokee.ide.visualstudio.VisualStudioIdeProjectConfiguration;
import lombok.val;
import org.gradle.api.Describable;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;

import static dev.nokee.internal.ProjectUtils.getPrefixableProjectPath;

/**
 * Task rule for bridging Xcode IDE with Gradle.
 */
public abstract class VisualStudioIdeBridge extends IdeBridgeRule<VisualStudioIdeRequest> {
	// TODO: Convert "format" into using a property set on the VS target
	public static final String BRIDGE_TASK_NAME = "_visualStudio__%s_$(ProjectName)_$(Configuration)_$(Platform)";
	private final NamedDomainObjectSet<VisualStudioIdeProject> visualStudioProjects;
	private final Project project;

	@Inject
	public VisualStudioIdeBridge(Describable ide, NamedDomainObjectSet<VisualStudioIdeProject> visualStudioProjects, Project project) {
		super(ide);
		this.visualStudioProjects = visualStudioProjects;
		this.project = project;
	}

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	protected String getLifecycleTaskNamePrefix() {
		return "_visualStudio";
	}

	@Override
	public VisualStudioIdeRequest newRequest(String taskName) {
		return getObjects().newInstance(VisualStudioIdeRequest.class, taskName);
	}

	@Override
	public void doHandle(VisualStudioIdeRequest request) {
		final DefaultVisualStudioIdeTarget target = findVisualStudioTarget(request);
		Copy bridgeTask = getTasks().create(request.getTaskName(), Copy.class); // TODO: We should sync but only the files we know about or redirect logs to another folder so we can sync
		bridgeProductBuild(bridgeTask, target, request);
	}

	private DefaultVisualStudioIdeTarget findVisualStudioTarget(VisualStudioIdeRequest request) {
		String projectName = request.getProjectName();
		DefaultVisualStudioIdeProject project = (DefaultVisualStudioIdeProject) visualStudioProjects.findByName(projectName);
		if (project == null) {
			throw new GradleException(String.format("Unknown Xcode IDE project '%s', try re-generating the Xcode IDE configuration using '%s:xcode' task.", projectName, getPrefixableProjectPath(this.project)));
		}

		val projectConfiguration = VisualStudioIdeProjectConfiguration.of(VisualStudioIdeConfiguration.of(request.getConfiguration()), VisualStudioIdePlatform.of(request.getPlatformName()));
		DefaultVisualStudioIdeTarget target = project.getTargets().stream().filter(it -> it.getProjectConfiguration().equals(projectConfiguration)).findFirst().orElse(null);
		if (target == null) {
			throw new GradleException(String.format("Unknown Xcode IDE target '%s', try re-generating the Xcode IDE configuration using '%s:xcode' task.", projectConfiguration, getPrefixableProjectPath(this.project)));
		}
		return target;
	}

	private void bridgeProductBuild(Copy bridgeTask, DefaultVisualStudioIdeTarget target, VisualStudioIdeRequest request) {
		// Library or executable
//			final String configurationName = request.getConfiguration();
//			XcodeIdeBuildConfiguration configuration = target.getBuildConfigurations().findByName(configurationName);
//			if (configuration == null) {
//				throw new GradleException(String.format("Unknown Xcode IDE configuration '%s', try re-generating the Xcode IDE configuration using '%s:xcode' task.", configurationName, getPrefixableProjectPath(this.project)));
//			}

		// For XCTest bundle, we have to copy the binary to the BUILT_PRODUCTS_DIR, otherwise Xcode doesn't find the tests.
		// For legacy target, we can work around this by setting the CONFIGURATION_BUILD_DIR to the parent of where Gradle put the built binaries.
		// However, XCTest target are a bit more troublesome and it doesn't seem to play by the same rules as the legacy target.
		// To simplify the configuration, let's always copy the product where Xcode expect it to be.
		// It remove complexity and fragility on the Gradle side.
		bridgeTask.from(target.getProductLocation());
		bridgeTask.setDestinationDir(request.getOutputDirectory().getAsFile());
	}
}
