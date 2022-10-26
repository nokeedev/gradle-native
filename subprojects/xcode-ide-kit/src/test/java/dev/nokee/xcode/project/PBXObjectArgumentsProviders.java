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
package dev.nokee.xcode.project;

import com.google.common.collect.ImmutableList;
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.objects.buildphase.PBXBuildPhase;
import dev.nokee.xcode.objects.configuration.BuildSettings;
import dev.nokee.xcode.objects.files.PBXSourceTree;
import dev.nokee.xcode.objects.targets.PBXTargetDependency;
import dev.nokee.xcode.objects.targets.ProductTypes;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.of;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;

public final class PBXObjectArgumentsProviders {
	public static final class PBXObjectNamesProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Stream.of(arguments((Object) null), arguments("CamelCaseName"), arguments("Space Name"), arguments("Hyphen-Name"));
		}
	}

	public static final class PBXReferencePathsProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Stream.of(arguments((Object) null), arguments("some/path/to/file.txt"));
		}
	}

	public static final class PBXReferenceSourceTreesProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Stream.of(arguments((Object) null), arguments(PBXSourceTree.SOURCE_ROOT), arguments(PBXSourceTree.of("FOO_BAR_DIR")));
		}
	}

	public static final class PBXTargetBuildFilesProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Stream.of(arguments((Object) null), arguments(ImmutableList.of(mock(PBXBuildFile.class), mock(PBXBuildFile.class), mock(PBXBuildFile.class))));
		}
	}

	public static final class PBXTargetBuildPhasesProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Stream.of(arguments((Object) null),
				arguments(of(mock(PBXBuildPhase.class), mock(PBXBuildPhase.class), mock(PBXBuildPhase.class))));
		}
	}

	public static final class PBXTargetDependenciesProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Stream.of(arguments((Object) null),
				arguments(of(mock(PBXTargetDependency.class), mock(PBXTargetDependency.class), mock(PBXTargetDependency.class))));
		}
	}

	public static final class PBXTargetProductNameProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Stream.of(arguments((Object) null), arguments("MyProductName"));
		}
	}

	public static final class PBXTargetProductTypesProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Stream.of(arguments((Object) null), arguments(ProductTypes.APPLICATION));
		}
	}

	public static final class BuildSettingsProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Stream.of(arguments((Object) null), arguments(BuildSettings.empty()), arguments(BuildSettings.builder().put("KEY1", "VALUE1").build()));
		}
	}
}
