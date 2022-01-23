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
package nokeebuild.ci;

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.GradlePluginTestingStrategy;
import nokeebuild.testing.strategies.DevelopmentTestingStrategy;
import nokeebuild.testing.strategies.OperatingSystemFamilyTestingStrategies;
import nokeebuild.testing.strategies.OperatingSystemFamilyTestingStrategy;
import org.apache.commons.lang3.SystemUtils;
import org.gradle.api.Transformer;
import org.gradle.api.internal.provider.Providers;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.testing.Test;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static dev.gradleplugins.GradlePluginTestingStrategy.Spec.matches;
import static dev.gradleplugins.GradlePluginTestingStrategy.testingStrategy;
import static java.util.function.Predicate.isEqual;
import static nokeebuild.testing.strategies.OperatingSystemFamilyTestingStrategyFactory.osFamilies;

class OperatingSystemFamilyTestTasksMapper implements GradlePluginDevelopmentTestSuiteTestTasksMapper {
	@Override
	public Provider<Iterable<Test>> apply(GradlePluginDevelopmentTestSuite testSuite, Provider<? extends Iterable<Test>> testTasks) {
        final OperatingSystemFamilyTestingStrategy agnostic = osFamilies(testSuite).getAgnostic();
		return testTasks.map(filtered(new TestPredicate(agnostic))).flatMap(Providers::of);
	}

    private static <IN> Transformer<Iterable<IN>, Iterable<? extends IN>> filtered(Predicate<? super IN> predicate) {
        return values -> StreamSupport.stream(values.spliterator(), false).filter(predicate).collect(Collectors.toList());
    }

	private static class TestPredicate implements Predicate<Test> {
        private final OperatingSystemFamilyTestingStrategy agnostic;

        public TestPredicate(OperatingSystemFamilyTestingStrategy agnostic) {
            this.agnostic = agnostic;
        }

        @Override
        public boolean test(Test task) {
            if (isCiEnvironment()) {
                if (isAgnosticOsFamily() && matches(isEqual(agnostic)).isSatisfiedBy(testingStrategy(task).getOrNull())) {
                    return true;
                }
				return matches(DevelopmentTestingStrategy.class::isInstance).negate().and(matches(currentOsFamily())).isSatisfiedBy(testingStrategy(task).getOrNull());
			} else {
                return matches(Objects::isNull).or(matches(DevelopmentTestingStrategy.class::isInstance)).isSatisfiedBy(testingStrategy(task).getOrNull());
            }
        }

        private static boolean isCiEnvironment() {
            return System.getenv().containsKey("CI");
        }

        private static boolean isAgnosticOsFamily() {
			final String osFamily = System.getProperty("nokeebuild.agnostic-os-family", "linux");
			switch (osFamily) {
				case "linux": return SystemUtils.IS_OS_LINUX;
				case "windows": return SystemUtils.IS_OS_WINDOWS;
				case "macos": return SystemUtils.IS_OS_MAC_OSX;
				default: throw new UnsupportedOperationException("Unknown agnostic OS family.");
			}
        }

        private static Predicate<GradlePluginTestingStrategy> currentOsFamily() {
			if (SystemUtils.IS_OS_WINDOWS) {
                return OperatingSystemFamilyTestingStrategies.WINDOWS::equals;
            } else if (SystemUtils.IS_OS_LINUX) {
                return OperatingSystemFamilyTestingStrategies.LINUX::equals;
            } else if (SystemUtils.IS_OS_MAC_OSX) {
				return OperatingSystemFamilyTestingStrategies.MACOS::equals;
			} else if (SystemUtils.IS_OS_FREE_BSD) {
				return OperatingSystemFamilyTestingStrategies.FREEBSD::equals;
            } else {
                throw new UnsupportedOperationException("Unknown current OS family");
            }
        }
    }
}
