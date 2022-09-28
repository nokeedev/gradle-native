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
package dev.nokee.buildadapter.xcode;

import com.google.common.testing.EqualsTester;
import dev.nokee.buildadapter.xcode.internal.plugins.DefaultXcodeInstallation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.nio.file.Paths;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.hasAbsolutePath;
import static dev.nokee.internal.testing.SerializableMatchers.isSerializable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class DefaultXcodeInstallationTests {
	@Nested
	class GivenSubject {
		DefaultXcodeInstallation subject = new DefaultXcodeInstallation("14.0.1", Paths.get("/Applications/Xcode.app/Contents/Developer"));

		@Test
		void hasVersion() {
			assertThat(subject.getVersion(), equalTo("14.0.1"));
		}

		@Test
		void hasDeveloperDirectory() {
			assertThat(subject.getDeveloperDirectory(),
				aFile(hasAbsolutePath("/Applications/Xcode.app/Contents/Developer")));
		}

		@Test
		void canSerialize() {
			assertThat(subject, isSerializable());
		}
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		// Note: Equality is based only on the version as the same Xcode installation could be in different location
		new EqualsTester()
			.addEqualityGroup(new DefaultXcodeInstallation("12.2", Paths.get("/Applications/Xcode.app/Contents/Developer")),
				new DefaultXcodeInstallation("12.2", Paths.get("/Applications/Xcode_12.2.app/Contents/Developer")))
			.addEqualityGroup(new DefaultXcodeInstallation("12.2.1", Paths.get("/Applications/Xcode.app/Contents/Developer")))
			.addEqualityGroup(new DefaultXcodeInstallation("13.3", Paths.get("/Applications/Xcode_13.3.app/Contents/Developer")))
			.testEquals();
	}
}
