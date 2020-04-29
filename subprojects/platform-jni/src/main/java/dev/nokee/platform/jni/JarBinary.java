package dev.nokee.platform.jni;

import dev.nokee.platform.base.Binary;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;

/**
 * Configuration for JAR binary.
 *
 * @since 0.3
 */
public interface JarBinary extends Binary {
	/**
	 * Returns the {@link Jar} task for this binary.
	 *
	 * @return a provider to the {@link Jar} task.
	 */
	TaskProvider<Jar> getJarTask();
}
