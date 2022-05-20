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
package dev.nokee.xcode.project;

import java.util.function.Consumer;

public final class PBXProj {

	private final PBXObjects objects;
	private final String rootObject;

	public PBXProj(PBXObjects objects, String rootObject) {
		this.objects = objects;
		this.rootObject = rootObject;
	}

	public PBXObjects getObjects() {
		return objects;
	}

	public String getRootObject() {
		return rootObject;
	}

	public static Builder builder() {
		return new Builder();
	}


	public static final class Builder {
		private PBXObjects objects;
		private String rootObject;

		public Builder objects(PBXObjects objects) {
			this.objects = objects;
			return this;
		}

		public Builder objects(Consumer<? super PBXObjects.Builder> builderConsumer) {
			final PBXObjects.Builder builder = PBXObjects.builder();
			builderConsumer.accept(builder);
			this.objects = builder.build();
			return this;
		}

		public Builder rootObject(String gid) {
			this.rootObject = gid;
			return this;
		}

		public PBXProj build() {
			if (objects == null) {
				objects = PBXObjects.builder().build();
			}
			return new PBXProj(objects, rootObject);
		}
	}
}
