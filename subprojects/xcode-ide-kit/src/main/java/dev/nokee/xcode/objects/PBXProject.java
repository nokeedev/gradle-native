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

import com.google.common.collect.ImmutableList;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.files.GroupChild;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXGroup;
import dev.nokee.xcode.objects.files.PBXSourceTree;
import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference;
import dev.nokee.xcode.objects.targets.PBXTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * The root object representing the project itself.
 */
public final class PBXProject extends PBXContainer implements PBXContainerItemProxy.ContainerPortal {
	private final PBXGroup mainGroup;
	private final List<PBXTarget> targets;
	private final XCConfigurationList buildConfigurationList;
	private final String compatibilityVersion;
	private final String name;
	private final List<ProjectReference> projectReferences;
	private final List<XCRemoteSwiftPackageReference> packageReferences;

	private PBXProject(String name, ImmutableList<PBXTarget> targets, XCConfigurationList buildConfigurationList, PBXGroup mainGroup, List<ProjectReference> projectReferences, List<XCRemoteSwiftPackageReference> packageReferences) {
		this.name = name;
		this.mainGroup = mainGroup;
		this.targets = targets;
		this.buildConfigurationList = buildConfigurationList;
		this.projectReferences = projectReferences;
		this.packageReferences = packageReferences;
		this.compatibilityVersion = "Xcode 3.2";
	}

	public String getName() {
		return name;
	}

	public PBXGroup getMainGroup() {
		return mainGroup;
	}

	public List<PBXTarget> getTargets() {
		return targets;
	}

	public XCConfigurationList getBuildConfigurationList() {
		return buildConfigurationList;
	}

	public String getCompatibilityVersion() {
		return compatibilityVersion;
	}

	public List<ProjectReference> getProjectReferences() {
		return projectReferences;
	}

	public List<XCRemoteSwiftPackageReference> getPackageReferences() {
		return packageReferences;
	}

	@Override
	public String toString() {
		return String.format("%s isa=%s", super.toString(), this.getClass().getSimpleName());
	}

	public static final class ProjectReference {
		private final PBXGroup productGroup;
		private final PBXFileReference projectReference;

		private ProjectReference(PBXGroup productGroup, PBXFileReference projectReference) {
			this.productGroup = productGroup;
			this.projectReference = projectReference;
		}

		public PBXGroup getProductGroup() {
			return productGroup;
		}

		public PBXFileReference getProjectReference() {
			return projectReference;
		}

		public static Builder builder() {
			return new Builder();
		}

		public static final class Builder {
			private PBXGroup productGroup;
			private PBXFileReference projectReference;

			public Builder productGroup(PBXGroup productGroup) {
				this.productGroup = productGroup;
				return this;
			}

			public Builder projectReference(PBXFileReference projectReference) {
				this.projectReference = projectReference;
				return this;
			}

			public ProjectReference build() {
				return new ProjectReference(productGroup, projectReference);
			}
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String name;
		private final List<PBXTarget> targets = new ArrayList<>();
		private XCConfigurationList buildConfigurations = XCConfigurationList.builder().build();
		private final List<GroupChild> mainGroupChildren = new ArrayList<>();
		private PBXGroup mainGroup;
		private final List<ProjectReference> projectReferences = new ArrayList<>();
		private final List<XCRemoteSwiftPackageReference> packageReferences = new ArrayList<>();

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder target(PBXTarget target) {
			targets.add(target);
			return this;
		}

		public Builder targets(Iterable<? extends PBXTarget> targets) {
			this.targets.clear();
			targets.forEach(this.targets::add);
			return this;
		}

		public Builder buildConfigurations(Consumer<? super XCConfigurationList.Builder> builderConsumer) {
			final XCConfigurationList.Builder builder = XCConfigurationList.builder();
			builderConsumer.accept(builder);
			this.buildConfigurations = builder.build();
			return this;
		}

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
			projectReferences.add(projectReference);
			return this;
		}

		public Builder projectReferences(Iterable<? extends ProjectReference> projectReferences) {
			this.projectReferences.clear();
			projectReferences.forEach(this.projectReferences::add);
			return this;
		}

		public Builder packageReference(XCRemoteSwiftPackageReference packageReference) {
			this.packageReferences.add(packageReference);
			return this;
		}

		public Builder packageReferences(Iterable<? extends XCRemoteSwiftPackageReference> packageReferences) {
			this.packageReferences.clear();
			packageReferences.forEach(this.packageReferences::add);
			return this;
		}

		public PBXProject build() {
			if (mainGroup == null) {
				this.mainGroup = PBXGroup.builder().name("mainGroup").sourceTree(PBXSourceTree.GROUP).children(mainGroupChildren).build();
			}
			return new PBXProject(name, ImmutableList.copyOf(targets), buildConfigurations, mainGroup, ImmutableList.copyOf(projectReferences), ImmutableList.copyOf(packageReferences));
		}
	}
}
