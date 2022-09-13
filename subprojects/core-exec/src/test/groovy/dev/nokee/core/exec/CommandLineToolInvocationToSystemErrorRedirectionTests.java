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
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static dev.nokee.core.exec.CommandLineToolInvocationOutputRedirection.toSystemError;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;

class CommandLineToolInvocationToSystemErrorRedirectionTests implements CommandLineToolInvocationOutputRedirectionTester<CommandLineToolInvocationOutputRedirection.ToSystemErrorRedirection> {
	CommandLineToolInvocationOutputRedirection.ToSystemErrorRedirection subject = new CommandLineToolInvocationOutputRedirection.ToSystemErrorRedirection();

	@Override
	public CommandLineToolInvocationOutputRedirection.ToSystemErrorRedirection subject() {
		return subject;
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public void testEquals(EqualsTester tester) {
		tester.addEqualityGroup(
			new CommandLineToolInvocationOutputRedirection.ToSystemErrorRedirection(),
			new CommandLineToolInvocationOutputRedirection.ToSystemErrorRedirection(),
			toSystemError()
		);
	}

	@Override
	public void testToString(String subjectToString) {
		assertThat(subjectToString, equalTo("toSystemErr()"));
	}

	@Test
	void returnsSystemErr() throws IOException {
		val savedSystemErr = System.err;
		try {
			val capturedOutput = new ByteArrayOutputStream();
			System.setErr(new PrintStream(capturedOutput));
			subject.redirect(Mockito.mock(CommandLineToolInvocationOutputRedirection.Context.class))
				.write("some error data".getBytes(UTF_8));
			assertThat(capturedOutput.toString(), equalTo("some error data"));
		} finally {
			System.setErr(savedSystemErr);
		}
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
