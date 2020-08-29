package dev.nokee.buildadapter.cmake.internal.plugins;

import dev.nokee.buildadapter.cmake.internal.DaggerCMakeBuilderAdapterComponent;
import dev.nokee.buildadapter.cmake.internal.fileapi.*;
import dev.nokee.buildadapter.cmake.internal.tasks.CMakeMSBuildAdapterTask;
import dev.nokee.buildadapter.cmake.internal.tasks.CMakeMakeAdapterTask;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ProjectIdentifier;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.nativebase.internal.dependencies.NativeOutgoingComponentDependencies;
import dev.nokee.runtime.base.internal.tools.ToolRepository;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.internal.os.OperatingSystem;

import java.util.Set;
import java.util.stream.Collectors;

public class CMakeBuildAdapterCodeModelVisitor implements CodeModelReplyFiles.Visitor {
	CodeModel.Configuration configuration = null;
	private final Settings settings;
	private final ProviderFactory providerFactory;
	private final ToolRepository repository;
	private CodeModel visitedCodeModel = null;

	public CMakeBuildAdapterCodeModelVisitor(Settings settings, ProviderFactory providerFactory, ToolRepository repository) {
		this.settings = settings;
		this.providerFactory = providerFactory;
		this.repository = repository;
	}

	public void configureGradleProjectLayout(CodeModel model) {
		Set<String> projects = model.getConfigurations().stream().flatMap(configuration -> {
			return configuration.getTargets().stream().map(CodeModel.Configuration.TargetReference::getName);
		}).collect(Collectors.toSet());

		projects.forEach(settings::include);
	}

	@Override
	public void visit(CodeModelReplyFile codemodelFile) {
		val codemodel = codemodelFile.get();
		visitedCodeModel = codemodel;
		configureGradleProjectLayout(codemodel);

		if (codemodel.getConfigurations().isEmpty()) {
			System.err.println("No CMake configuration found!");
		} else if (codemodel.getConfigurations().size() == 1) {
			configuration = codemodel.getConfigurations().iterator().next();
		} else {
			configuration = codemodel.getConfigurations().stream().filter(it -> it.getName().toLowerCase().equals("release")).findFirst().orElse(null);
			if (configuration == null) {
				System.err.println(String.format("No CMake release configuration found, possible choices was: '%s'.", codemodel.getConfigurations().stream().map(CodeModel.Configuration::getName).collect(Collectors.joining("','"))));
			}
		}
	}

	@Override
	public void visit(CodeModelTargetReplyFile codeModelTargetFile) {
		assert visitedCodeModel != null;
		if (!getConfiguration(codeModelTargetFile).getName().equals(configuration.getName())) {
			return;
		}

		settings.getGradle().rootProject(rootProject -> {
			rootProject.project(getProjectPath(codeModelTargetFile), configureTargetProject(codeModelTargetFile));
		});
	}

	private Action<Project> configureTargetProject(CodeModelTargetReplyFile codeModelTargetFile) {
		return project -> {
			val targetModel = codeModelTargetFile.get();
			if (!targetModel.getType().equals("STATIC_LIBRARY")) {
				project.getLogger().error(String.format("Unsupported target type of '%s' on project '%s', supported target type is 'STATIC_LIBRARY'.", targetModel.getType(), project.getPath()));
				return;
			}

			// Singletons
			val factory = DaggerCMakeBuilderAdapterComponent.factory().create(project).dependenciesContainerFactory();

			// Per component
			val dependencies = factory.create(new VariantIdentifier(configuration.getName(), new ComponentIdentifier("main", ProjectIdentifier.of(project))));
			val outgoingDependencies = new NativeOutgoingComponentDependencies(dependencies);

			// A typical static library
			if (!targetModel.getCompileGroups().isEmpty()) {
				val compileGroup = targetModel.getCompileGroups().iterator().next(); // Assuming only one
				compileGroup.getIncludes().stream().map(CodeModelTarget.CompileGroup.Include::getPath).map(it -> project.getRootProject().file(it)).forEach(outgoingDependencies.getCompileElements()::headerSearchPath);
			} else { // We may have a header-only library in the form of a "fake" target to overcome the "pseudo-target limitation".
				// Find the lowest common folder of all headers in the sources
				// TODO: use C/C++ headers extensions from UTType
				val includePath = targetModel.getSources().stream().map(CodeModelTarget.Source::getPath).filter(it -> it.endsWith(".h")).map(it -> it.substring(0, FilenameUtils.indexOfLastSeparator(it))).reduce((a, b) -> {
					if (a.length() < b.length()) {
						return a;
					}
					return b;
				}).<String>map(it -> it.substring(0, FilenameUtils.indexOfLastSeparator(it)));
				outgoingDependencies.getCompileElements().headerSearchPath(includePath.map(it -> project.getRootProject().file(it)).get());
			}

			// Compile only if there are compile groups
			if (!targetModel.getCompileGroups().isEmpty()) {
				if (OperatingSystem.current().isWindows()) {
					val makeTask = project.getTasks().register("make", CMakeMSBuildAdapterTask.class, task -> {
						task.getTargetName().set(targetModel.getName());
						task.getConfigurationName().set(configuration.getName());
						task.getBuiltFile().set(project.getRootProject().file(targetModel.getArtifacts().iterator().next().getPath()));
						task.getWorkingDirectory().set(project.getRootProject().getLayout().getProjectDirectory());
						task.getMsbuildTool().set(providerFactory.provider(() -> CommandLineTool.of(repository.findAll("msbuild").iterator().next().getPath())));
					});
					outgoingDependencies.getLinkElements().staticLibraryArtifact(makeTask.flatMap(CMakeMSBuildAdapterTask::getBuiltFile));
				} else {
					val makeTask = project.getTasks().register("make", CMakeMakeAdapterTask.class, task -> {
						task.getTargetName().set(targetModel.getName());
						task.getBuiltFile().set(project.getRootProject().file(targetModel.getArtifacts().iterator().next().getPath()));
						task.getWorkingDirectory().set(project.getRootProject().getLayout().getProjectDirectory());
						task.getMakeTool().set(providerFactory.provider(() -> CommandLineTool.of(repository.findAll("make").iterator().next().getPath())));
					});
					outgoingDependencies.getLinkElements().staticLibraryArtifact(makeTask.flatMap(CMakeMakeAdapterTask::getBuiltFile));
				}
			}
		};
	}

	private CodeModel.Configuration getConfiguration(CodeModelTargetReplyFile file) {
		for (CodeModel.Configuration configuration : visitedCodeModel.getConfigurations()) {
			if (configuration.getTargets().stream().anyMatch(it -> it.getJsonFile().equals(file.getReplyFile().getName()))) {
				return configuration;
			}
		}
		throw new RuntimeException("Not found");
	}

	String getProjectPath(CodeModelTargetReplyFile file) {
		return ":" + file.get().getName();
	}

	@Override
	public void visit(IndexReplyFile replyIndex) {
		// Do nothing.
	}
}
