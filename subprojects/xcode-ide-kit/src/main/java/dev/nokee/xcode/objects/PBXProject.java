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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
			private PBXGroup productGroup;
			private PBXFileReference projectReference;
			private final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();

			public Builder() {
				builder.put(KeyedCoders.ISA, null);
			}

			@Override
			public Builder lenient() {
				builder.lenient();
				return this;
			}

			public Builder productGroup(PBXGroup productGroup) {
				this.productGroup = productGroup;
				return this;
			}

			public Builder projectReference(PBXFileReference projectReference) {
				this.projectReference = projectReference;
				return this;
			}

			@Override
			public ProjectReference build() {
				builder.put(CodeableProjectReference.CodingKeys.ProductGroup, productGroup);
				builder.put(CodeableProjectReference.CodingKeys.ProjectRef, projectReference);

				return new CodeableProjectReference(builder.build());
			}
		}
	}

	static Builder builder() {
		return new Builder();
	}

	final class Builder implements org.apache.commons.lang3.builder.Builder<PBXProject>, BuildConfigurationsAwareBuilder<Builder>, SelfConfigurationAwareBuilder<Builder>, LenientAwareBuilder<Builder> {
		@Nullable private final KeyedObject parent;
		private List<PBXTarget> targets;
		private XCConfigurationList buildConfigurations;
		private final List<GroupChild> mainGroupChildren = new ArrayList<>();
		private PBXGroup mainGroup;
		private List<ProjectReference> projectReferences;
		private List<XCRemoteSwiftPackageReference> packageReferences;
		private final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();

		public Builder() {
			this.parent = null;
			builder.put(KeyedCoders.ISA, "PBXProject");
		}

		public Builder(KeyedObject parent) {
			this.parent = parent;
			builder.parent(parent);
			builder.put(KeyedCoders.ISA, "PBXProject");
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

		public Builder target(PBXTarget target) {
			assert target != null : "'target' must not be null";
			if (targets == null) {
				this.targets = new ArrayList<>();
			}
			targets.add(target);
			return this;
		}

		public Builder targets(PBXTarget... targets) {
			return targets(Arrays.asList(targets));
		}

		public Builder targets(Iterable<? extends PBXTarget> targets) {
			this.targets = new ArrayList<>();
			targets.forEach(this.targets::add);
			return this;
		}

		@Override
		public Builder buildConfigurations(XCConfigurationList buildConfigurations) {
			this.buildConfigurations = Objects.requireNonNull(buildConfigurations);
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
			return this;
		}

		public Builder projectReference(ProjectReference projectReference) {
			if (projectReferences == null) {
				projectReferences = new ArrayList<>();
			}
			projectReferences.add(projectReference);
			return this;
		}

		public Builder projectReferences(Iterable<? extends ProjectReference> projectReferences) {
			this.projectReferences = new ArrayList<>();
			projectReferences.forEach(this.projectReferences::add);
			return this;
		}

		public Builder packageReference(XCRemoteSwiftPackageReference packageReference) {
			if (packageReferences == null) {
				packageReferences = new ArrayList<>();
			}
			this.packageReferences.add(packageReference);
			return this;
		}

		public Builder packageReferences(Iterable<? extends XCRemoteSwiftPackageReference> packageReferences) {
			this.packageReferences = new ArrayList<>();
			packageReferences.forEach(this.packageReferences::add);
			return this;
		}

		@Override
		public PBXProject build() {
			if (mainGroup == null && parent == null) {
				this.mainGroup = PBXGroup.builder().name("mainGroup").sourceTree(PBXSourceTree.GROUP).children(mainGroupChildren).build();
			}

			builder.put(CodeablePBXProject.CodingKeys.targets, targets);
			builder.put(CodeablePBXProject.CodingKeys.buildConfigurationList, buildConfigurations);
			builder.put(CodeablePBXProject.CodingKeys.mainGroup, mainGroup);
			builder.put(CodeablePBXProject.CodingKeys.projectReferences, projectReferences);
			builder.put(CodeablePBXProject.CodingKeys.packageReferences, packageReferences);

			return CodeablePBXProject.newInstance(builder.build());
		}
	}
}
