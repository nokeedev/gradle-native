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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;

class CommandLineToolInvocationInheritedStandardOutputRedirectionTests implements CommandLineToolInvocationOutputRedirectionTester<CommandLineToolInvocationInheritedStandardOutputRedirection> {
	CommandLineToolInvocationInheritedStandardOutputRedirection subject = new CommandLineToolInvocationInheritedStandardOutputRedirection();

	@Override
	public CommandLineToolInvocationInheritedStandardOutputRedirection subject() {
		return subject;
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public void testEquals(EqualsTester tester) {
		tester.addEqualityGroup(
			new CommandLineToolInvocationInheritedStandardOutputRedirection(),
			new CommandLineToolInvocationInheritedStandardOutputRedirection()
		);
	}

	@Override
	public void testToString(String subjectToString) {
		assertThat(subjectToString, equalTo("inherited()"));
	}

	@Test
	void returnsInheritedOutput() {
		val context = Mockito.mock(CommandLineToolInvocationOutputRedirection.Context.class);
		Mockito.when(context.getStandardOutput()).thenReturn(System.out);
		assertThat(subject.redirect(context), is(System.out));
		Mockito.verify(context).getStandardOutput();
	}

	@Test
	void isNotErrorOutputRedirect() {
		assertThat(subject, not(isA(CommandLineToolInvocationErrorOutputRedirect.class)));
	}

	@Test
	void isStandardOutputRedirect() {
		assertThat(subject, isA(CommandLineToolInvocationStandardOutputRedirect.class));
	}
}
