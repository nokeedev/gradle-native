/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.buildadapter.xcode.internal.plugins;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static dev.nokee.buildadapter.xcode.internal.plugins.RedirectProjectBuildDirectoryToRootBuildDirectory.BuildDirectoryService.registerService;
import static dev.nokee.utils.ProjectUtils.isRootProject;
import static dev.nokee.utils.ProviderUtils.forUseAtConfigurationTime;

public final class RedirectProjectBuildDirectoryToRootBuildDirectory implements Action<Project> {
	private static final String BUILD_PATH_PREFIX = "subprojects/";
	private static final char BUILD_PATH_SEPARATOR_CHAR = '-';
	private final HashFunction algorithm;

	public RedirectProjectBuildDirectoryToRootBuildDirectory() {
		this(djb2());
	}

	public RedirectProjectBuildDirectoryToRootBuildDirectory(HashFunction algorithm) {
		this.algorithm = algorithm;
	}

	@Override
	public void execute(Project rootProject) {
		assert isRootProject(rootProject) : "can only execute on root project";

		final Provider<BuildDirectoryService> service = forUseAtConfigurationTime(registerService(rootProject.getGradle(), parameters -> parameters.getBuildDirectoriesCacheFile().set(rootProject.getLayout().getBuildDirectory().file(BUILD_PATH_PREFIX + "mapping.ser"))));

		rootProject.subprojects(project -> {
			// Note: We get the mapped service provider because Gradle is having trouble serializing the provider.
			// TODO: We should change our strategy to compute a unique build directory path
			project.getLayout().getBuildDirectory().set(rootProject.getLayout().getBuildDirectory().dir(service.map(it -> BUILD_PATH_PREFIX + it.computeIfAbsent(project, algorithm::hash)).get()));
		});
	}

	public interface HashFunction {
		long hash(String str);
	}

	public static HashFunction djb2() {
		return new Djb2HashAlgorithm();
	}

	private static final class Djb2HashAlgorithm implements HashFunction {
		@Override
		public long hash(String str) {
			return hash(str.toCharArray());
		}

		private long hash(char[] str) {
			long hash = 5381;

			int i = 0;
			while (i < str.length) {
				int c = str[i++];
				hash = ((hash << 5) + hash) + c; /* hash * 33 + c */
			}

			return hash;
		}
	}

	@SuppressWarnings("UnstableApiUsage")
	static abstract class BuildDirectoryService implements BuildService<BuildDirectoryService.Parameters>, AutoCloseable {
		interface Parameters extends BuildServiceParameters {
			RegularFileProperty getBuildDirectoriesCacheFile();
		}

		private final Map<String, String> projectToBuildPathMapping;
		private final Set<String> knownBuildPaths = new HashSet<>();
		private boolean isDirty = false;

		@Inject
		public BuildDirectoryService() {
			this.projectToBuildPathMapping = load(getParameters().getBuildDirectoriesCacheFile().getAsFile().get());
			this.knownBuildPaths.addAll(projectToBuildPathMapping.values());
		}

		private static Map<String, String> load(File cacheFile) {
			try (final ObjectInputStream inStream = new ObjectInputStream(new FileInputStream(cacheFile))) {
				@SuppressWarnings("unchecked")
				final Map<String, String> result = (Map<String, String>) inStream.readObject();
				return result;
			} catch (Exception e) {
				return new LinkedHashMap<>(); // just assume a new cache
			}
		}

		public String computeIfAbsent(Project project, Function<? super String, ? extends Long> mapper) {
			if (projectToBuildPathMapping.containsKey(project.getPath())) {
				return projectToBuildPathMapping.get(project.getPath());
			} else {
				String result = null;
				for (int i = 0; result == null || !knownBuildPaths.add(result); ++i) {
					long hash = mapper.apply(project.getPath() + "[" + i + "]");
					result = project.getName() + BUILD_PATH_SEPARATOR_CHAR + Long.toUnsignedString(hash, Character.MAX_RADIX);
				}
				projectToBuildPathMapping.put(project.getPath(), result);
				isDirty = true;
				return result;
			}
		}

		@Override
		public void close() {
			if (isDirty) {
				save(getParameters().getBuildDirectoriesCacheFile().getAsFile().get(), projectToBuildPathMapping);
			}
		}

		private static void save(File cacheFile, Map<String, String> cache) {
			try (final ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream(cacheFile))) {
				outStream.writeObject(cache);
			} catch (Exception e) {
				// just ignore saving the cache
			}
		}

		public static Provider<BuildDirectoryService> registerService(Gradle gradle, Action<? super Parameters> action) {
			return gradle.getSharedServices().registerIfAbsent("buildDirectoriesService", BuildDirectoryService.class, registration -> {
				registration.parameters(action);
			});
		}
	}
}
