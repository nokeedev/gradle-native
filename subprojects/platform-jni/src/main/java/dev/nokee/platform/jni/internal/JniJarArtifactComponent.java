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
package dev.nokee.platform.jni.internal;

import dev.nokee.model.internal.core.LinkedEntity;
import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.jni.JniJarBinary;
import dev.nokee.platform.jni.JvmJarBinary;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.bundling.Jar;

import java.nio.file.Path;
import java.util.concurrent.Callable;

public final class JniJarArtifactComponent implements Callable<Object>, ModelComponent, LinkedEntity {
	public final ModelNode entity;

	public JniJarArtifactComponent(ModelNode entity) {
		this.entity = entity;
	}

	public Provider<Path> getJarFile() {
		return ModelNodeUtils.get(entity, JniJarBinary.class).getJarTask().flatMap(it -> it.getArchiveFile()).map(it -> it.getAsFile().toPath());
	}

	public Provider<Jar> getJarTask() {
		return ModelNodeUtils.get(entity, JniJarBinary.class).getJarTask();//.flatMap(it -> it.getArchiveFile()).map(it -> it.getAsFile().toPath());
	}

	@Override
	public Object call() throws Exception {
		ModelStates.realize(entity);
		return ModelNodeUtils.get(entity, JniJarBinary.class);
	}

	public ModelNode get() {
		return entity;
	}
}
