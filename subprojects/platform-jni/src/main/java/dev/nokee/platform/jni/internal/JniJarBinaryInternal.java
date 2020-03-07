package dev.nokee.platform.jni.internal;

import dev.nokee.platform.base.internal.BinaryInternal;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform;

import javax.inject.Inject;

public abstract class JniJarBinaryInternal extends BinaryInternal {
	private final TaskProvider<Jar> jarTask;

	@Inject
	public JniJarBinaryInternal(TaskContainer tasks) {
		this.jarTask = tasks.register("jniJar", Jar.class, task -> {
			task.getArchiveBaseName().set(task.getProject().getName() + "-" + DefaultNativePlatform.getCurrentOperatingSystem().toFamilyName() + "-" + DefaultNativePlatform.getCurrentArchitecture().getName());
		});
	}

	public TaskProvider<Jar> getJarTask() {
		return jarTask;
	}

	public abstract ConfigurableFileCollection getSource();

	public Provider<RegularFile> getArchiveFile() {
		return this.jarTask.flatMap(Jar::getArchiveFile);
	}
}
