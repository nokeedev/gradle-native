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
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static dev.nokee.core.exec.CommandLineToolInvocationOutputRedirection.toFile;
import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.anExistingFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withTextContent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;

@ExtendWith(TestDirectoryExtension.class)
class CommandLineToolInvocationToFileRedirectionTests implements CommandLineToolInvocationOutputRedirectionTester<CommandLineToolInvocationOutputRedirection.ToFileRedirection> {
	@TestDirectory Path testDirectory;
	CommandLineToolInvocationOutputRedirection.ToFileRedirection subject;

	@BeforeEach
	void createSubject() {
		subject = new CommandLineToolInvocationOutputRedirection.ToFileRedirection(testDirectory.resolve("out.log"));
	}

	@Override
	public CommandLineToolInvocationOutputRedirection.ToFileRedirection subject() {
		return subject;
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public void testEquals(EqualsTester tester) {
		tester.addEqualityGroup(
			new CommandLineToolInvocationOutputRedirection.ToFileRedirection(testDirectory.resolve("foo")),
			new CommandLineToolInvocationOutputRedirection.ToFileRedirection(testDirectory.resolve("foo")),
			toFile(testDirectory.resolve("foo"))
		);
		tester.addEqualityGroup(new CommandLineToolInvocationOutputRedirection.ToFileRedirection(testDirectory.resolve("bar")));
	}

	@Override
	public void testToString(String subjectToString) {
		assertThat(subjectToString, equalTo("toFile(" + testDirectory.resolve("out.log") + ")"));
	}

	@Test
	void returnsOutputStreamOfFile() throws IOException {
		try (val outStream = subject.redirect(Mockito.mock(CommandLineToolInvocationOutputRedirection.Context.class))) {
			outStream.write("some output data".getBytes(StandardCharsets.UTF_8));
		}
		assertThat(testDirectory.resolve("out.log"), anExistingFile());
		assertThat(testDirectory.resolve("out.log"), aFile(withTextContent(equalTo("some output data"))));
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
