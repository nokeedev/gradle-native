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
package dev.nokee.core.exec;

import com.google.common.testing.EqualsTester;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.core.exec.CommandLineToolInvocationOutputRedirection.toNullStream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;

class CommandLineToolInvocationToNullStreamRedirectionTests implements CommandLineToolInvocationOutputRedirectionTester<CommandLineToolInvocationOutputRedirection.ToNullStreamRedirection> {
	CommandLineToolInvocationOutputRedirection.ToNullStreamRedirection subject = new CommandLineToolInvocationOutputRedirection.ToNullStreamRedirection();

	@Override
	public CommandLineToolInvocationOutputRedirection.ToNullStreamRedirection subject() {
		return subject;
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public void testEquals(EqualsTester tester) {
		tester.addEqualityGroup(
			new CommandLineToolInvocationOutputRedirection.ToNullStreamRedirection(),
			new CommandLineToolInvocationOutputRedirection.ToNullStreamRedirection(),
			toNullStream()
		);
	}

	@Override
	public void testToString(String subjectToString) {
		assertThat(subjectToString, equalTo("toNullStream()"));
	}

	@Test
	void returnsNullStream() {
		assertThat(subject.redirect(Mockito.mock(CommandLineToolInvocationOutputRedirection.Context.class)),
			isA(NullOutputStream.class));
	}

	@Test
	void isErrorOutputRedirect() {
		assertThat(subject, isA(CommandLineToolInvocationErrorOutputRedirect.class));
	}

	@Test
	void isStandardOutputRedirect() {
		assertThat(subject, isA(CommandLineToolInvocationStandardOutputRedirect.class));
	}
}
