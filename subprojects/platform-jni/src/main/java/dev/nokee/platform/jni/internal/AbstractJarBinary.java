package dev.nokee.platform.jni.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.platform.base.Binary;
import lombok.EqualsAndHashCode;
import org.gradle.api.Buildable;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;

@EqualsAndHashCode
public abstract class AbstractJarBinary implements Binary, Buildable {
	private final TaskProvider<Jar> jarTask;

	public AbstractJarBinary(TaskProvider<Jar> jarTask) {
		this.jarTask = jarTask;
	}

	public TaskProvider<Jar> getJarTask() {
		return jarTask;
	}

	public Provider<RegularFile> getArchiveFile() {
		return this.jarTask.flatMap(Jar::getArchiveFile);
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return task -> ImmutableSet.of(jarTask.get());
	}
}
