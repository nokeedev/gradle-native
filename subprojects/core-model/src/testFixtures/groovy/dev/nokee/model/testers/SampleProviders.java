/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.model.testers;

public final class SampleProviders<T> {
	private final SampleProvider<T> p0;
	private final SampleProvider<T> p1;
	private final SampleProvider<T> p2;


	public SampleProviders(SampleProvider<T> p0, SampleProvider<T> p1, SampleProvider<T> p2) {
		this.p0 = p0;
		this.p1 = p1;
		this.p2 = p2;
	}

	public SampleProvider<T> p0() {
		return p0;
	}

	public SampleProvider<T> p1() {
		return p1;
	}

	public SampleProvider<T> p2() {
		return p2;
	}
}
