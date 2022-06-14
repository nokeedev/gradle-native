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
package dev.nokee.nvm;

import dev.nokee.nvm.fixtures.TestGradleBuild;
import dev.nokee.nvm.fixtures.TestLayout;

import java.util.function.Consumer;

import static dev.nokee.nvm.ProjectFixtures.nokeeBuild;
import static dev.nokee.nvm.ProjectFixtures.nonNokeeBuild;

public final class NokeeVersionManagementServiceSamples {
	public static SingleBuildLayout singleNokeeBuild(TestLayout layout) {
		layout.configure(nokeeBuild());
		return new SingleBuildLayout(layout);
	}

	public static final class SingleBuildLayout {
		private final TestLayout delegate;

		private SingleBuildLayout(TestLayout delegate) {
			this.delegate = delegate;
		}

		public SingleBuildLayout rootBuild(Consumer<? super TestGradleBuild> consumer) {
			delegate.configure(consumer);
			return this;
		}
	}

	public static BuildChildOfBuildLayout nokeeBuildChildOfNonNokeeBuild(TestLayout layout) {
		layout.configure(nonNokeeBuild().parentOf("A", nokeeBuild()));
		return new BuildChildOfBuildLayout(layout);
	}

	public static BuildChildOfBuildLayout nokeeBuildChildOfNokeeBuild(TestLayout layout) {
		layout.configure(nokeeBuild().parentOf("A", nokeeBuild()));
		return new BuildChildOfBuildLayout(layout);
	}

	public static final class BuildChildOfBuildLayout {
		private final TestLayout delegate;

		public BuildChildOfBuildLayout(TestLayout delegate) {
			this.delegate = delegate;
		}

		public BuildChildOfBuildLayout rootBuild(Consumer<? super TestGradleBuild> consumer) {
			delegate.configure(consumer);
			return this;
		}

		public BuildChildOfBuildLayout childBuild(Consumer<? super TestGradleBuild> consumer) {
			delegate.includeBuild("A", consumer);
			return this;
		}
	}
}
