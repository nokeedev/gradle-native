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

import java.util.Collections;
import java.util.function.Consumer;

/**
 * Concrete target type representing targets built by xcode itself, rather than an external build
 * system.
 */
public final class PBXLegacyTarget extends PBXTarget {
    private String buildArgumentsString;
    private String buildToolPath;
    private String buildWorkingDirectory;
    private boolean passBuildSettingsInEnvironment;

	private PBXLegacyTarget(String name, String productType, String productName, PBXFileReference productReference, XCConfigurationList buildConfigurationList, String buildArgumentsString, String buildToolPath, String buildWorkingDirectory, boolean passBuildSettingsInEnvironment) {
		super(name, productType, Collections.emptyList(), buildConfigurationList, productName, productReference);
		this.buildArgumentsString = buildArgumentsString;
		this.buildToolPath = buildToolPath;
		this.buildWorkingDirectory = buildWorkingDirectory;
		this.passBuildSettingsInEnvironment = passBuildSettingsInEnvironment;
	}

	public String getBuildArgumentsString() {
        return buildArgumentsString;
    }

    public void setBuildArgumentsString(String buildArgumentsString) {
        this.buildArgumentsString = buildArgumentsString;
    }

    public String getBuildToolPath() {
        return buildToolPath;
    }

    public void setBuildToolPath(String buildToolPath) {
        this.buildToolPath = buildToolPath;
    }

    public String getBuildWorkingDirectory() {
        return buildWorkingDirectory;
    }

    public void setBuildWorkingDirectory(String buildWorkingDirectory) {
        this.buildWorkingDirectory = buildWorkingDirectory;
    }

    public boolean isPassBuildSettingsInEnvironment() {
        return passBuildSettingsInEnvironment;
    }

    public void setPassBuildSettingsInEnvironment(boolean passBuildSettingsInEnvironment) {
        this.passBuildSettingsInEnvironment = passBuildSettingsInEnvironment;
    }

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String name;
		private String productType;
		private String buildArgumentsString = "$(ACTION)";
		private String buildToolPath = "/usr/bin/make";
		private String buildWorkingDirectory;
		private boolean passBuildSettingsInEnvironment = true;
		private XCConfigurationList buildConfigurationList;
		private String productName;
		private PBXFileReference productReference;

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder productType(String productType) {
			this.productType = productType;
			return this;
		}

		public Builder buildConfigurations(Consumer<? super XCConfigurationList.Builder> builderConsumer) {
			final XCConfigurationList.Builder builder = XCConfigurationList.builder();
			builderConsumer.accept(builder);
			this.buildConfigurationList = builder.build();
			return this;
		}

		public Builder productName(String productName) {
			this.productName = productName;
			return this;
		}

		public Builder productReference(PBXFileReference productReference) {
			this.productReference = productReference;
			return this;
		}

		public Builder buildArguments(String buildArguments) {
			this.buildArgumentsString = buildArguments;
			return this;
		}

		public Builder buildToolPath(String buildToolPath) {
			this.buildToolPath = buildToolPath;
			return this;
		}

		public Builder buildWorkingDirectory(String buildWorkingDirectory) {
			this.buildWorkingDirectory = buildWorkingDirectory;
			return this;
		}

		public Builder passBuildSettingsInEnvironment(boolean passBuildSettingsInEnvironment) {
			this.passBuildSettingsInEnvironment = passBuildSettingsInEnvironment;
			return this;
		}

		public PBXLegacyTarget build() {
			return new PBXLegacyTarget(name, productType, productName, productReference, buildConfigurationList, buildArgumentsString, buildToolPath, buildWorkingDirectory, passBuildSettingsInEnvironment);
		}
	}
}
