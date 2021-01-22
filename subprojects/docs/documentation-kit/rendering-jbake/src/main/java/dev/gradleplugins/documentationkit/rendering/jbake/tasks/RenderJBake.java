package dev.gradleplugins.documentationkit.rendering.jbake.tasks;

import dev.gradleplugins.documentationkit.rendering.jbake.tasks.internal.JBakeWorkAction;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.*;
import org.gradle.workers.ClassLoaderWorkerSpec;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.IOException;

public abstract class RenderJBake extends DefaultTask {
	private final WorkerExecutor workers;

	@InputDirectory
	public abstract DirectoryProperty getSourceDirectory();

	@Input
	public abstract MapProperty<String, Object> getConfigurations();

	@Classpath
	public abstract ConfigurableFileCollection getClasspath();

	@OutputDirectory
	public abstract DirectoryProperty getDestinationDirectory();

	@Inject
	public RenderJBake(WorkerExecutor workers) {
		this.workers = workers;
	}

	@TaskAction
	private void doRender() throws IOException {
		FileUtils.cleanDirectory(getDestinationDirectory().get().getAsFile());
		workers.classLoaderIsolation(this::configureClasspath).submit(JBakeWorkAction.class, this::configureAction);
	}

	private void configureClasspath(ClassLoaderWorkerSpec spec) {
		spec.getClasspath().from(getClasspath());
	}

	private void configureAction(JBakeWorkAction.Parameters parameters) {
		parameters.getSourceDirectory().set(getSourceDirectory());
		parameters.getDestinationDirectory().set(getDestinationDirectory());
		parameters.getConfigurations().set(getConfigurations());
	}

}
