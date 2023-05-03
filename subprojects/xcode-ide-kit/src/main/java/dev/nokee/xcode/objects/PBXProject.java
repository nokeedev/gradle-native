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
package dev.nokee.xcode.objects;

import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.files.GroupChild;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXGroup;
import dev.nokee.xcode.objects.files.PBXSourceTree;
import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference;
import dev.nokee.xcode.objects.targets.BuildConfigurationsAwareBuilder;
import dev.nokee.xcode.objects.targets.PBXTarget;
import dev.nokee.xcode.objects.targets.SelfConfigurationAwareBuilder;
import dev.nokee.xcode.project.CodeablePBXProject;
import dev.nokee.xcode.project.CodeableProjectReference;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;
import dev.nokee.xcode.project.KeyedObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * The root object representing the project itself.
 */
public interface PBXProject extends PBXContainer, PBXContainerItemProxy.ContainerPortal {
	PBXGroup getMainGroup();

	List<PBXTarget> getTargets();

	XCConfigurationList getBuildConfigurationList();

	String getCompatibilityVersion();

	List<ProjectReference> getProjectReferences();

	List<XCRemoteSwiftPackageReference> getPackageReferences();

	Builder toBuilder();

	interface ProjectReference {
		PBXGroup getProductGroup();

		PBXFileReference getProjectReference();

		static Builder builder() {
			return new Builder();
		}

		final class Builder implements org.apache.commons.lang3.builder.Builder<ProjectReference>, LenientAwareBuilder<Builder> {
			private final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder()
				.knownKeys(CodeableProjectReference.CodingKeys.values());

			public Builder() {
				// There is no ISA value for this model
			}

			@Override
			public Builder lenient() {
				builder.lenient();
				return this;
			}

			public Builder productGroup(PBXGroup productGroup) {
				builder.put(CodeableProjectReference.CodingKeys.ProductGroup, productGroup);
				return this;
			}

			public Builder projectReference(PBXFileReference projectReference) {
				builder.put(CodeableProjectReference.CodingKeys.ProjectRef, projectReference);
				return this;
			}

			@Override
			public ProjectReference build() {
				return new CodeableProjectReference(builder.build());
			}
		}
	}

	static Builder builder() {
		return new Builder();
	}

	final class Builder implements org.apache.commons.lang3.builder.Builder<PBXProject>, BuildConfigurationsAwareBuilder<Builder>, SelfConfigurationAwareBuilder<Builder>, LenientAwareBuilder<Builder> {
		private final List<GroupChild> mainGroupChildren = new ArrayList<>();
		private PBXGroup mainGroup;
		private final DefaultKeyedObject.Builder builder;

		public Builder() {
			this(new DefaultKeyedObject.Builder().knownKeys(KeyedCoders.ISA).knownKeys(CodeablePBXProject.CodingKeys.values()));
		}

		public Builder(KeyedObject parent) {
			this(new DefaultKeyedObject.Builder().parent(parent));
		}

		private Builder(DefaultKeyedObject.Builder builder) {
			this.builder = builder.put(KeyedCoders.ISA, "PBXProject")
				.ifAbsent(CodeablePBXProject.CodingKeys.mainGroup, PBXGroup.builder().name("mainGroup").sourceTree(PBXSourceTree.GROUP).children(mainGroupChildren).build());
		}

		@Override
		public Builder lenient() {
			builder.lenient();
			return this;
		}

		@Override
		public Builder with(UnaryOperator<Builder> action) {
			return action.apply(this);
		}

		public Builder projectDirPath(String path) {
			builder.put(CodeablePBXProject.CodingKeys.projectDirPath, path);
			return this;
		}

		public Builder target(PBXTarget target) {
			builder.add(CodeablePBXProject.CodingKeys.targets, target);
			return this;
		}

		public Builder targets(PBXTarget... targets) {
			return targets(Arrays.asList(targets));
		}

		public Builder targets(Iterable<? extends PBXTarget> targets) {
			builder.put(CodeablePBXProject.CodingKeys.targets, targets);
			return this;
		}

		@Override
		public Builder buildConfigurations(XCConfigurationList buildConfigurations) {
			builder.put(CodeablePBXProject.CodingKeys.buildConfigurationList, buildConfigurations);
			return this;
		}

		public Builder file(PBXFileReference fileReference) {
			mainGroupChildren.add(fileReference);
			return this;
		}

		public Builder group(Consumer<? super PBXGroup.Builder> builderConsumer) {
			final PBXGroup.Builder builder = PBXGroup.builder();
			builderConsumer.accept(builder);
			mainGroupChildren.add(builder.build());
			mainGroup = null;
			return this;
		}

		public Builder mainGroup(PBXGroup mainGroup) {
			this.mainGroup = mainGroup;
			this.mainGroupChildren.clear();
			builder.put(CodeablePBXProject.CodingKeys.mainGroup, mainGroup);
			return this;
		}

		public Builder projectReference(ProjectReference projectReference) {
			builder.add(CodeablePBXProject.CodingKeys.projectReferences, projectReference);
			return this;
		}

		public Builder projectReferences(Iterable<? extends ProjectReference> projectReferences) {
			builder.put(CodeablePBXProject.CodingKeys.projectReferences, projectReferences);
			return this;
		}

		public Builder packageReference(XCRemoteSwiftPackageReference packageReference) {
			builder.add(CodeablePBXProject.CodingKeys.packageReferences, packageReference);
			return this;
		}

		public Builder packageReferences(Iterable<? extends XCRemoteSwiftPackageReference> packageReferences) {
			builder.put(CodeablePBXProject.CodingKeys.packageReferences, packageReferences);
			return this;
		}

		@Override
		public PBXProject build() {
			if (mainGroup == null && !mainGroupChildren.isEmpty()) {
				builder.put(CodeablePBXProject.CodingKeys.mainGroup, PBXGroup.builder().name("mainGroup").sourceTree(PBXSourceTree.GROUP).children(mainGroupChildren).build());
			}

			return CodeablePBXProject.newInstance(builder.build());
		}
	}
}
