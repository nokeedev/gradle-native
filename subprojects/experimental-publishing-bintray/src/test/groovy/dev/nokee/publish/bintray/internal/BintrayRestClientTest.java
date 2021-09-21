/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.publish.bintray.internal;

import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.file.RegularFile;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import spock.lang.Subject;

import java.io.File;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.publish.bintray.internal.BintrayCredentials.of;
import static dev.nokee.publish.bintray.internal.BintrayPackageName.of;
import static dev.nokee.publish.bintray.internal.BintrayTestUtils.defaultUrl;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.io.FileMatchers.aFileWithAbsolutePath;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Subject(BintrayRestClient.class)
class BintrayRestClientTest {
	@TempDir
	protected File testDirectory;

	@Test
	void canPutBintrayArtifact() {
		val workQueue = mock(WorkQueue.class);
		val workers = mock(WorkerExecutor.class);
		when(workers.noIsolation()).thenReturn(workQueue);
		val parameters = objectFactory().newInstance(HttpPutWorkAction.Parameters.class);
		doAnswer(invocation -> {
			invocation.getArgument(1, Action.class).execute(parameters);
			return null;
		}).when(workQueue).submit(eq(HttpPutWorkAction.class), any());

		val subject = new BintrayRestClient(workers);
		subject.put(new BintrayArtifact(new File(testDirectory, "foo"), "some/path", "4.2"), new BintrayRepository(defaultUrl(), of("foo"), of("user", "key")));

		assertThat(parameters.getUsername(), providerOf(equalTo("user")));
		assertThat(parameters.getPassword(), providerOf(equalTo("key")));
		assertThat(parameters.getRelativePath(), providerOf(equalTo("some/path")));
		assertThat(parameters.getFile(), providerOf(fileAt(testDirectory.getAbsolutePath() + File.separator + "foo")));
		assertThat(parameters.getUrl(), providerOf(hasToString("https://api.bintray.com/content/nokeedev/examples/foo/4.2/some/path?publish=1&override=0")));
	}

	private static Matcher<RegularFile> fileAt(String path) {
		return new FeatureMatcher<RegularFile, File>(aFileWithAbsolutePath(equalTo(path)), "", "") {

			@Override
			protected File featureValueOf(RegularFile actual) {
				return actual.getAsFile();
			}
		};
	}

	// TODO: Test override -Dforce flag
}
