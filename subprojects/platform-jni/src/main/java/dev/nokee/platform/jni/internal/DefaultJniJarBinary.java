package dev.nokee.platform.jni.internal;

import dev.nokee.platform.jni.JniJarBinary;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;

import javax.inject.Inject;

public abstract class DefaultJniJarBinary extends AbstractJarBinary implements JniJarBinary {
	@Inject
	public DefaultJniJarBinary(TaskProvider<Jar> jarTask) {
		super(jarTask);
	}
}
