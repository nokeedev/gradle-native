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
package dev.nokee.fixtures.exec;

import dev.nokee.core.exec.CommandLineToolInvocation;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.nokee.core.exec.CommandLineToolInvocationOutputRedirection.toNullStream;
import static org.hamcrest.Matchers.contains;

public final class CommandLineToolMatchers {
	private CommandLineToolMatchers() {}

	public static Matcher<CommandLineToolInvocation> commandLine(Matcher<? super Iterable<String>> matcher) {
		return new FeatureMatcher<CommandLineToolInvocation, Iterable<String>>(matcher, "", "") {

			@Override
			protected Iterable<String> featureValueOf(CommandLineToolInvocation actual) {
				List<String> result = new ArrayList<>();
				result.add(actual.getExecutable().getLocation().toString());
				actual.getArguments().forEach(result::add);
				return result;
			}
		};
	}

	public static Matcher<CommandLineToolInvocation> discardedOutputs() {
		return new FeatureMatcher<CommandLineToolInvocation, Iterable<Object>>(contains(toNullStream(), toNullStream()), "", "") {
			@Override
			protected Iterable<Object> featureValueOf(CommandLineToolInvocation actual) {
				return Arrays.asList(actual.getStandardOutputRedirect(), actual.getErrorOutputRedirect());
			}
		};
	}
}
