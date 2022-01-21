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
package nokeebuild.jvm;

import org.gradle.api.Named;

import java.util.Objects;

public final class XlintWarning implements Named {
	private final String name;

	private XlintWarning(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof XlintWarning)) {
			return false;
		}
		XlintWarning that = (XlintWarning) o;
		return Objects.equals(getName(), that.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName());
	}

	public static XlintWarning named(String name) {
		return new XlintWarning(name);
	}
}
