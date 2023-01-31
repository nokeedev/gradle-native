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
package dev.nokee.xcode;

import com.google.common.collect.ImmutableMap;
import dev.nokee.buildadapter.xcode.internal.plugins.specs.XCBuildSpec;
import dev.nokee.xcode.objects.buildphase.BuildFileAwareBuilder;
import dev.nokee.xcode.objects.buildphase.PBXBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXFrameworksBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXHeadersBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXResourcesBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXSourcesBuildPhase;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference;
import dev.nokee.xcode.objects.swiftpackage.XCSwiftPackageProductDependency;
import dev.nokee.xcode.objects.targets.PBXAggregateTarget;
import dev.nokee.xcode.objects.targets.PBXLegacyTarget;
import dev.nokee.xcode.objects.targets.PBXNativeTarget;
import dev.nokee.xcode.objects.targets.PBXTarget;
import dev.nokee.xcode.objects.targets.PBXTargetDependency;
import dev.nokee.xcode.objects.targets.ProductType;
import dev.nokee.xcode.project.Codeable;
import dev.nokee.xcode.project.CodeablePBXNativeTarget;
import dev.nokee.xcode.project.CodingKey;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;
import lombok.val;
import org.apache.commons.lang3.builder.Builder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.of;
import static dev.nokee.buildadapter.xcode.TestProjectReference.project;
import static dev.nokee.xcode.objects.buildphase.PBXBuildFile.ofFile;
import static dev.nokee.xcode.objects.buildphase.PBXBuildFile.ofProduct;
import static dev.nokee.xcode.objects.files.PBXFileReference.ofSourceRoot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

@ExtendWith(MockitoExtension.class)
class XCBuildSpecLoaderTests {
	XCTargetReference reference = project("Test").ofTarget("Test");
	PBXTarget target;
	@Mock XCLoader<PBXTarget, XCTargetReference> targetLoader;
	@Mock XCLoader<XCFileReferencesLoader.XCFileReferences, XCProjectReference> fileRefsLoader;
	XCBuildSpecLoader subject;

	@BeforeEach
	void givenTargetLoaded() {
		target = new TestTargetAdapter() {};
		subject = new XCBuildSpecLoader(targetLoader); // TODO: Pass mock for XCBuildSpecFactory
		Mockito.when(targetLoader.load(reference)).thenAnswer(args -> target);
//		Mockito.when(fileRefsLoader.load(reference.getProject())).thenReturn(XCFileReferencesLoader.XCFileReferences.builder().put(ofSourceRoot("foo/bar.c"), XCFileReference.fromBuildSetting("SOURCE_ROOT", "foo/bar.c")).build());
	}

	Map<String, ?> result() {
		val result = flatten(subject.load(reference));
		System.out.println(result);
		return result;
	}

	@Nested
	class WhenPBXAggregateTarget {
		@BeforeEach
		void givenTarget() {
			target = PBXAggregateTarget.builder().lenient().build();
		}

		@Test
		void includeTargetType() {
			assertThat(result(), hasEntry("isa", "PBXAggregateTarget"));
		}
	}

	@Nested
	class WhenPBXLegacyTarget {
		@BeforeEach
		void givenTarget() {
			target = PBXLegacyTarget.builder().lenient().build();
		}

		@Test
		void includeTargetType() {
			assertThat(result(), hasEntry("isa", "PBXLegacyTarget"));
		}
	}

	@Nested
	class WhenPBXNativeTarget {
		@BeforeEach
		void givenTarget() {
			target = PBXNativeTarget.builder().lenient().build();
		}

		@Test
		void includeTargetType() {
			assertThat(result(), hasEntry("isa", "PBXNativeTarget"));
		}
	}

	@Nested
	class WhenTargetHasNoBuildPhases {
		@BeforeEach
		void givenNoTargets() {
			target = new TestTargetAdapter() {
				@Override
				public List<PBXBuildPhase> getBuildPhases() {
					return Collections.emptyList();
				}
			};
		}

		@Test
		void canLoadTargetSpec() {
			assertThat(result(), not(hasKey(startsWith("buildPhases."))));
		}
	}

	@Nested
	class WhenTargetHasBuildPhases {
		List<PBXBuildPhase> buildPhases;

		@BeforeEach
		void givenTargetsWithBuildPhases() {
			target = new TestTargetAdapter() {
				@Override
				public List<PBXBuildPhase> getBuildPhases() {
					return buildPhases;
				}
			};
		}

		@Test
		void canLoadTargetWithSingleBuildPhases() {
			buildPhases = of(PBXSourcesBuildPhase.builder().build());
			assertThat(result(), hasKey(startsWith("buildPhases.0.")));
		}

		@Test
		void canLoadTargetWithMultipleBuildPhases() {
			buildPhases = of(PBXSourcesBuildPhase.builder().build(), PBXSourcesBuildPhase.builder().build(), PBXSourcesBuildPhase.builder().build());
			assertThat(result(), allOf(hasKey(startsWith("buildPhases.0.")), hasKey(startsWith("buildPhases.1.")), hasKey(startsWith("buildPhases.2."))));
		}

		abstract class BuildFilesTester<SUBJECT_TYPE extends PBXBuildPhase, BUILDER_TYPE extends BuildFileAwareBuilder<BUILDER_TYPE> & Builder<SUBJECT_TYPE>> {
			abstract BUILDER_TYPE builder();

			@BeforeEach
			void givenBuildPhases() {
				buildPhases = of(builder().file(ofFile(ofSourceRoot("foo/bar.c"))).file(ofProduct(XCSwiftPackageProductDependency.builder().packageReference(XCRemoteSwiftPackageReference.builder().requirement(XCRemoteSwiftPackageReference.VersionRequirement.range("3.4", "3.6")).repositoryUrl("https://github.com/examplecom/Foo").build()).productName("Foo").build())).build());
			}

			@Test
			void canFilesOfBuildPhases() {
				assertThat(result(), hasEntry("buildPhases.0.files.0.fileRef", "$(SOURCE_ROOT)/foo/bar.c"));
			}

			@Test
			void canProductOfBuildPhases() {
				assertThat(result(), hasEntry("buildPhases.0.files.1.productRef.package.requirement.kind", "versionRange"));
				assertThat(result(), hasEntry("buildPhases.0.files.1.productRef.package.requirement.minimumVersion", "3.4"));
				assertThat(result(), hasEntry("buildPhases.0.files.1.productRef.package.requirement.maximumVersion", "3.6"));
				assertThat(result(), hasEntry("buildPhases.0.files.1.productRef.package.repositoryURL", "https://github.com/examplecom/Foo")); // TODO: Normalize repo if Xcode treat similar URL the same
				assertThat(result(), hasEntry("buildPhases.0.files.1.productRef.productName", "Foo"));
			}
		}

		@Nested
		class WhenSingleSourcesBuildPhase extends BuildFilesTester<PBXSourcesBuildPhase, PBXSourcesBuildPhase.Builder>  {
			@Override
			PBXSourcesBuildPhase.Builder builder() {
				return PBXSourcesBuildPhase.builder().lenient();
			}
		}

		@Nested
		class WhenSingleCopyFilesBuildPhase extends BuildFilesTester<PBXCopyFilesBuildPhase, PBXCopyFilesBuildPhase.Builder>  {
			@Override
			PBXCopyFilesBuildPhase.Builder builder() {
				return PBXCopyFilesBuildPhase.builder().lenient().destination(it -> it.executables("Test"));
			}
		}

		@Nested
		class WhenSingleFrameworkBuildPhase extends BuildFilesTester<PBXFrameworksBuildPhase, PBXFrameworksBuildPhase.Builder>  {
			@Override
			PBXFrameworksBuildPhase.Builder builder() {
				return PBXFrameworksBuildPhase.builder().lenient();
			}
		}

		@Nested
		class WhenSingleHeadersBuildPhase extends BuildFilesTester<PBXHeadersBuildPhase, PBXHeadersBuildPhase.Builder>  {
			@Override
			PBXHeadersBuildPhase.Builder builder() {
				return PBXHeadersBuildPhase.builder().lenient();
			}
		}

		@Nested
		class WhenSingleResourcesBuildPhase extends BuildFilesTester<PBXResourcesBuildPhase, PBXResourcesBuildPhase.Builder>  {
			@Override
			PBXResourcesBuildPhase.Builder builder() {
				return PBXResourcesBuildPhase.builder().lenient();
			}
		}
	}

	private static Map<String, ?> flatten(XCBuildSpec spec) {
		ImmutableMap.Builder<String, Object> result = ImmutableMap.builder();
		spec.visit(new XCBuildSpec.Visitor() {
			private final Deque<String> contexts = new ArrayDeque<>();

			@Override
			public void visitValue(Object value) {
				result.put(String.join(".", contexts), value);
			}

			@Override
			public void enterContext(String namespace) {
				contexts.addLast(namespace);
			}

			@Override
			public void exitContext() {
				contexts.removeLast();
			}
		});
		return result.build();
	}

	private static abstract class TestTargetAdapter implements PBXTarget, Codeable {
		@Override
		public String getName() {
			return "Test";
		}

		@Override
		public Optional<ProductType> getProductType() {
			return Optional.empty();
		}

		@Override
		public List<PBXBuildPhase> getBuildPhases() {
			return Collections.emptyList();
		}

		@Override
		public XCConfigurationList getBuildConfigurationList() {
			return XCConfigurationList.builder().lenient().build();
		}

		@Override
		public List<PBXTargetDependency> getDependencies() {
			return Collections.emptyList();
		}

		@Override
		public Optional<String> getProductName() {
			return Optional.empty();
		}

		@Override
		public Optional<PBXFileReference> getProductReference() {
			return Optional.empty();
		}

		@Override
		public String isa() {
			return "PBXTarget";
		}

		@Nullable
		@Override
		public String globalId() {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T tryDecode(CodingKey key) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void encode(EncodeContext context) {
			val builder = new DefaultKeyedObject.Builder().put(KeyedCoders.ISA, isa());
			getProductType().ifPresent(it -> builder.put(CodeablePBXNativeTarget.CodingKeys.productType, it));
			getProductName().ifPresent(it -> builder.put(CodeablePBXNativeTarget.CodingKeys.productName, it));
			getProductReference().ifPresent(it -> builder.put(CodeablePBXNativeTarget.CodingKeys.productReference, it));
			builder.put(CodeablePBXNativeTarget.CodingKeys.productType, getBuildConfigurationList());
			builder.put(CodeablePBXNativeTarget.CodingKeys.name, getName());
			builder.put(CodeablePBXNativeTarget.CodingKeys.dependencies, getDependencies());
			builder.put(CodeablePBXNativeTarget.CodingKeys.buildPhases, getBuildPhases());
			builder.build().encode(context);
		}
	}
}
