/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.docs;

import dev.nokee.docs.fixtures.ClassSource;
import dev.nokee.docs.fixtures.LinkCheckerUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;

import java.io.File;
import java.net.URI;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static dev.nokee.docs.fixtures.HttpRequestMatchers.document;
import static dev.nokee.docs.fixtures.HttpRequestMatchers.statusCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Tag("Baked")
class BrokenLinksTest {
	@EnabledOnOs(OS.LINUX)
	@ParameterizedTest(name = "check URL [{0}]")
	@ClassSource(BakedContentSupplier.class)
	void checksHtmlForBrokenLinks(URI context) {
		if (context.getScheme().equals("mailto")) {
			assertThat(context.toString(), equalTo("mailto:hello@nokee.dev"));
		} else {
			Assumptions.assumeFalse(context.getPath().contains("IgnoreEmptyDirectories.html"));
			Assumptions.assumeFalse(context.getPath().contains("UntrackedTask.html"));
			assertThat(context, statusCode(anyOf(Matchers.is(200), Matchers.is(301))));
			if (context.getPath().contains("#")) {
				String id = context.getPath().substring(context.getPath().lastIndexOf('#'));
				assertThat(document(context).getElementById(id), notNullValue());
			}
		}
	}

	public static final class BakedContentSupplier implements Supplier<Stream<URI>> {
		@Override
		public Stream<URI> get() {
			return LinkCheckerUtils.findAllLinks(new File(System.getProperty("bakedContentDirectory")).toPath());
		}
	}
}
