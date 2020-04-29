package dev.nokee.platform.jni.internal;

import dev.nokee.platform.jni.JniJarBinary;
import dev.nokee.platform.jni.JvmJarBinary;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;

import javax.inject.Inject;

public abstract class DefaultJvmJarBinary extends AbstractJarBinary implements JvmJarBinary {
	@Inject
	public DefaultJvmJarBinary(TaskProvider<Jar> jarTask) {
		super(jarTask);
	}
}
