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
package dev.nokee.buildadapter.cocoapods.internal;

import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;
import org.gradle.api.provider.ValueSourceSpec;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("UnstableApiUsage")
final class CocoaPodsProviders {
	private final ProviderFactory providers;

	public CocoaPodsProviders(ProviderFactory providers) {
		this.providers = providers;
	}

	public Provider<Podfile> podfile(File path) {
		return providers.of(PodfileLoader.class, parameters(it -> it.getPodfileFile().set(path)));
	}

	public Provider<CocoaPodsInstallation> installation(Action<? super CocoaPodsInstallationSpec> action) {
		return providers.of(CocoaPodsInstallationSource.class, parameters(action));
	}

	public interface CocoaPodsInstallationSpec {
		Property<Podfile> getPodfile();
		RegularFileProperty getCacheFile();
	}

	abstract static class PodfileLoader implements ValueSource<Podfile, PodfileLoader.Parameters> {
		interface Parameters extends ValueSourceParameters {
			RegularFileProperty getPodfileFile();
		}

		@Inject
		public PodfileLoader() {}

		@Nullable
		@Override
		public Podfile obtain() {
			val location = getParameters().getPodfileFile().getAsFile().getOrNull();
			if (location != null) {
				return Podfile.of(location);
			}
			return null;
		}
	}

	private static <T extends ValueSourceParameters> Action<ValueSourceSpec<T>> parameters(Action<? super T> action) {
		return spec -> spec.parameters(action);
	}

	abstract static class CocoaPodsInstallationSource implements ValueSource<CocoaPodsInstallation, CocoaPodsInstallationSource.Parameters> {
		private static final CocoaPodsService service = new CocoaPodsService();
		private final CocoaPods pod;

		interface Parameters extends ValueSourceParameters, CocoaPodsInstallationSpec {
			Property<Podfile> getPodfile();

			RegularFileProperty getCacheFile();
		}

		@Inject
		public CocoaPodsInstallationSource() {
			this.pod = new CachingCocoaPods(DefaultCocoaPods.inDirectory(getWorkingDirectory().toFile()), getParameters().getCacheFile().getAsFile().get());
		}

		private Path getWorkingDirectory() {
			return getParameters().getPodfile().get().getLocation().getParent();
		}

		private Path getPodfileLockFile() {
			return getWorkingDirectory().resolve("Podfile.lock");
		}

		private Podfile getPodfileFile() {
			return getParameters().getPodfile().get();
		}

		@Nullable
		@Override
		public CocoaPodsInstallation obtain() {
			pod.ifOutOfDate(it -> {
				service.install(getPodfileFile().getLocation().getParent());
			});

			// CocoaPods could be disabled (no Podfile) and still has Podfile.lock
			if (!pod.isEnabled()) {
				return null;
			} else {
				try {
					byte[] podfileLockContent = Files.readAllBytes(getPodfileLockFile());
					// only need to capture Podfile.lock and manifest.lock has the same content after install
					return new CocoaPodsInstallation(podfileLockContent);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}
		}
	}
}
