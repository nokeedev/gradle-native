package dev.nokee.gradle.internal;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dev.nokee.gradle.GradleProjectGroup;
import dev.nokee.gradle.GradleProjectVersion;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.component.SoftwareComponentFactory;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.process.ExecOperations;
import org.gradle.workers.WorkerExecutor;

@Module
public interface GradleModule {
	@Provides
	static ObjectFactory theObjectFactory(Project project) {
		return project.getObjects();
	}

	@Provides
	static ProviderFactory theProviderFactory(Project project) {
		return project.getProviders();
	}

	@Provides
	static ProjectLayout theProjectLayout(Project project) {
		return project.getLayout();
	}

	@Provides
	static ConfigurationContainer theConfigurationContainer(Project project) {
		return project.getConfigurations();
	}

	@Provides
	static DependencyHandler theDependencyHandler(Project project) {
		return project.getDependencies();
	}

	@Provides
	static FileSystemOperations theFileSystemOperations(Project project) {
		return ((ProjectInternal) project).getServices().get(FileSystemOperations.class);
	}

	@Provides
	static ExecOperations theExecOperations(Project project) {
		return ((ProjectInternal) project).getServices().get(ExecOperations.class);
	}

	@Provides
	static ExtensionContainer theExtensionContainer(Project project) {
		return project.getExtensions();
	}

	@Provides
	static TaskContainer theTaskContainer(Project project) {
		return project.getTasks();
	}

	@Provides
	static SoftwareComponentFactory theSoftwareComponentFactory(Project project) {
		return ((ProjectInternal) project).getServices().get(SoftwareComponentFactory.class);
	}

	@Provides
	static WorkerExecutor theWorkerExecutor(Project project) {
		return ((ProjectInternal) project).getServices().get(WorkerExecutor.class);
	}

	@Binds
	GradleProjectGroup theGradleProjectGroup(GradleProjectGroupImpl impl);

	@Binds
	GradleProjectVersion theGradleProjectVersion(GradleProjectVersionImpl impl);
}
