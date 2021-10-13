/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.platform.jni.internal.plugins;

import com.google.common.collect.ImmutableList;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import lombok.val;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.internal.jvm.Jvm;
import org.gradle.nativeplatform.platform.NativePlatform;

import java.io.File;
import java.util.List;
import java.util.function.Function;

import static dev.nokee.platform.jni.internal.plugins.NativeCompileTaskProperties.targetPlatformProperty;

public final class JvmIncludeRoots {
	private JvmIncludeRoots() {}

	//region JVM headers
	public static <SELF extends Task> Function<SELF, Provider<List<File>>> jvmIncludes() {
		return task -> targetPlatformProperty(task).map(toJvmIncludes());
	}

	private static Transformer<List<File>, NativePlatform> toJvmIncludes() {
		return platform -> jvmIncludes(OperatingSystemFamily.forName(platform.getOperatingSystem().getName()));
	}

	private static List<File> jvmIncludes(OperatingSystemFamily osFamily) {
		val result = ImmutableList.<File>builder();
		result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include"));

		if (osFamily.isMacOs()) {
			result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include/darwin"));
		} else if (osFamily.isLinux()) {
			result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include/linux"));
		} else if (osFamily.isWindows()) {
			result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include/win32"));
		} else if (osFamily.isFreeBSD()) {
			result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include/freebsd"));
		}
		return result.build();
	}
	//endregion
}
