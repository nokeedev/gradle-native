package dev.nokee.platform.jni.internal;

import dev.nokee.platform.jni.JvmJarBinary;
import lombok.EqualsAndHashCode;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;

import javax.inject.Inject;

@EqualsAndHashCode(callSuper = true)
public class DefaultJvmJarBinary extends AbstractJarBinary implements JvmJarBinary {
	@Inject
	public DefaultJvmJarBinary(TaskProvider<Jar> jarTask) {
		super(jarTask);
	}
}
