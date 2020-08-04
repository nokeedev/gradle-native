package dev.nokee.ide.visualstudio.internal.tasks;

import dev.nokee.ide.visualstudio.VisualStudioIdeProjectReference;
import dev.nokee.ide.visualstudio.internal.DefaultVisualStudioIdeGuid;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public abstract class GenerateVisualStudioIdeSolutionTask extends DefaultTask {
	@Nested
	public abstract SetProperty<VisualStudioIdeProjectReference> getProjectReferences();

	@OutputFile
	public abstract RegularFileProperty getSolutionLocation();

	@Inject
	public GenerateVisualStudioIdeSolutionTask() {
		dependsOn(getProjectReferences());
	}

	@TaskAction
	private void doGenerate() throws FileNotFoundException {

		try (PrintWriter out = new PrintWriter(getSolutionLocation().get().getAsFile())) {
			out.println("Microsoft Visual Studio Solution File, Format Version 12.00");
			out.println("# Visual Studio Version 16");
			out.println("VisualStudioVersion = 16.0.30225.117");
			out.println("MinimumVisualStudioVersion = 10.0.40219.1");
			getProjectReferences().get().forEach(reference -> {
				out.println("Project(\"{8BC9CEB8-8B4A-11D0-8D11-00A0C91BC942}\") = \"" + FilenameUtils.removeExtension(reference.getProjectLocation().get().getAsFile().getName()) + "\", \"" + reference.getProjectLocation().get().getAsFile().getAbsolutePath() + "\", \"" + reference.getProjectGuid().get().toString() + "\"");
				out.println("EndProject");
			});
			out.println("Global");
			out.println("	GlobalSection(SolutionConfigurationPlatforms) = preSolution");
			out.println("		Default|x64 = Default|x64");
			out.println("	EndGlobalSection");
			out.println("	GlobalSection(ProjectConfigurationPlatforms) = postSolution");
			getProjectReferences().get().forEach(info -> {
				out.println(String.format("		%s.Default|x64.ActiveCfg = Default|x64", info.getProjectGuid().get().toString()));
				out.println(String.format("		%s.Default|x64.Build.0 = Default|x64", info.getProjectGuid().get().toString()));
			});
			out.println("	EndGlobalSection");
			out.println("	GlobalSection(SolutionProperties) = preSolution");
			out.println("		HideSolutionNode = FALSE");
			out.println("	EndGlobalSection");
			out.println("	GlobalSection(ExtensibilityGlobals) = postSolution");
			out.println(String.format("		SolutionGuid = %s", DefaultVisualStudioIdeGuid.stableGuidFrom(getSolutionLocation()).getAsString()));
			out.println("	EndGlobalSection");
			out.println("EndGlobal");
		}
	}
}
