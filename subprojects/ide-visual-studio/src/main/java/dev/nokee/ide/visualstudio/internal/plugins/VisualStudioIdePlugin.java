package dev.nokee.ide.visualstudio.internal.plugins;

import dev.nokee.ide.base.internal.IdeProjectExtension;
import dev.nokee.ide.base.internal.IdeProjectInternal;
import dev.nokee.ide.base.internal.IdeWorkspaceExtension;
import dev.nokee.ide.base.internal.plugins.AbstractIdePlugin;
import dev.nokee.ide.visualstudio.VisualStudioIdeProject;
import dev.nokee.ide.visualstudio.VisualStudioIdeProjectExtension;
import dev.nokee.ide.visualstudio.internal.*;
import dev.nokee.internal.Cast;
import dev.nokee.platform.base.internal.Component;
import dev.nokee.platform.base.internal.ComponentCollection;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import lombok.val;
import org.gradle.api.file.RegularFile;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.plugins.ide.internal.IdeProjectMetadata;

import java.util.stream.Collectors;

public abstract class VisualStudioIdePlugin extends AbstractIdePlugin<VisualStudioIdeProject> {
	public static final String VISUAL_STUDIO_EXTENSION_NAME = "visualStudio";

	@Override
	protected void doProjectApply(IdeProjectExtension<VisualStudioIdeProject> extension) {
		extension.getProjects().withType(DefaultVisualStudioIdeProject.class).configureEach(xcodeProject -> {
			xcodeProject.getGeneratorTask().configure( task -> {
				RegularFile projectLocation = getLayout().getProjectDirectory().file(xcodeProject.getName() + ".vcxproj");
				task.getProjectLocation().set(projectLocation);
//				task.usesService(xcodeIdeGidGeneratorService);
//				task.getGidGenerator().set(xcodeIdeGidGeneratorService);
				task.getGradleCommand().set(toGradleCommand(getProject().getGradle()));
//				task.getBridgeTaskPath().set(toBridgeTaskPath(getProject()));
				task.getAdditionalGradleArguments().set(getAdditionalBuildArguments());
//				task.getSources().from(getBuildFiles());
			});
		});

		getProject().getPluginManager().withPlugin("dev.nokee.cpp-application", this::registerNativeProjects);
	}

	private void registerNativeProjects(AppliedPlugin appliedPlugin) {
		getProject().getExtensions().getByType(VisualStudioIdeProjectExtension.class).getProjects().register(getProject().getName(), visualStudioProject -> {
			ComponentCollection<Component> components = Cast.uncheckedCast("of type erasure", getProject().getExtensions().getByType(ComponentCollection.class));
			components.configureEach(DefaultNativeApplicationComponent.class, application -> {
				val visualStudioProjectInternal = (DefaultVisualStudioIdeProject)visualStudioProject;

				application.getSourceCollection().forEach(sourceSet -> {
					visualStudioProjectInternal.getSourceFiles().from(sourceSet.getAsFileTree());
				});
				visualStudioProjectInternal.getHeaderFiles().from(getProject().fileTree("src/main/headers", it -> it.include("*")));
				visualStudioProjectInternal.getBuildFiles().from(getBuildFiles());
			});
		});
	}

	@Override
	protected void doWorkspaceApply(IdeWorkspaceExtension<VisualStudioIdeProject> extension) {
		DefaultVisualStudioIdeWorkspaceExtension workspaceExtension = (DefaultVisualStudioIdeWorkspaceExtension) extension;

		workspaceExtension.getWorkspace().getGeneratorTask().configure(task -> {
			task.getSolutionLocation().set(getLayout().getProjectDirectory().file(getProject().getName() + ".sln"));
			task.getProjectInformations().set(getArtifactRegistry().getIdeProjects(VisualStudioIdeProjectMetadata.class).stream().map(it -> new VisualStudioIdeProjectInformation(it.get())).collect(Collectors.toList()));
		});
	}

	@Override
	protected IdeWorkspaceExtension<VisualStudioIdeProject> newIdeWorkspaceExtension() {
		return getObjects().newInstance(DefaultVisualStudioIdeWorkspaceExtension.class);
	}

	@Override
	protected IdeProjectExtension<VisualStudioIdeProject> newIdeProjectExtension() {
		return getObjects().newInstance(DefaultVisualStudioIdeProjectExtension.class);
	}

	@Override
	protected IdeProjectMetadata newIdeProjectMetadata(Provider<IdeProjectInternal> ideProject) {
		return new VisualStudioIdeProjectMetadata(ideProject);
	}

	@Override
	protected String getExtensionName() {
		return VISUAL_STUDIO_EXTENSION_NAME;
	}
}
