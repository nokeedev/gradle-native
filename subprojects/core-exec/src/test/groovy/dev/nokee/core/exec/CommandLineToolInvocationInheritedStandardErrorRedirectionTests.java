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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;

class CommandLineToolInvocationInheritedStandardErrorRedirectionTests implements CommandLineToolInvocationOutputRedirectionTester<CommandLineToolInvocationInheritedStandardErrorRedirection> {
	CommandLineToolInvocationInheritedStandardErrorRedirection subject = new CommandLineToolInvocationInheritedStandardErrorRedirection();

	@Override
	public CommandLineToolInvocationInheritedStandardErrorRedirection subject() {
		return subject;
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public void testEquals(EqualsTester tester) {
		tester.addEqualityGroup(
			new CommandLineToolInvocationInheritedStandardErrorRedirection(),
			new CommandLineToolInvocationInheritedStandardErrorRedirection()
		);
	}

	@Override
	public void testToString(String subjectToString) {
		assertThat(subjectToString, equalTo("inherited()"));
	}

	@Test
	void returnsInheritedOutput() {
		val context = Mockito.mock(CommandLineToolInvocationOutputRedirection.Context.class);
		Mockito.when(context.getErrorOutput()).thenReturn(System.err);
		assertThat(subject.redirect(context), is(System.err));
		Mockito.verify(context).getErrorOutput();
	}

	@Test
	void isErrorOutputRedirect() {
		assertThat(subject, isA(CommandLineToolInvocationErrorOutputRedirect.class));
	}

	@Test
	void isNotStandardOutputRedirect() {
		assertThat(subject, not(isA(CommandLineToolInvocationStandardOutputRedirect.class)));
	}
}
