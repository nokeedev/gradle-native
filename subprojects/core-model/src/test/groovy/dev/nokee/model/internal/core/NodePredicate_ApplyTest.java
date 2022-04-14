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
package dev.nokee.model.internal.core;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelTestActions.doSomething;
import static dev.nokee.model.internal.core.ModelTestActions.doSomethingElse;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

class NodePredicate_ApplyTest {
	@Test
	void canApplyPredicateToModelActionWhichCanBeScopedToAnyPaths() {
		val paths = new ArrayList<ModelPath>();
		val scopedAction = allDirectDescendants().apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPathComponent.class), (node, path) -> paths.add(ModelNodeUtils.getPath(node)))).scope(path("foo"));
		scopedAction.execute(node("foo.bar"));
		scopedAction.execute(node("bar"));

		assertThat(paths, contains(path("foo.bar")));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(allDirectDescendants().apply(doSomething()), allDirectDescendants().apply(doSomething()))
			.addEqualityGroup(allDirectDescendants().apply(doSomethingElse()))
			.addEqualityGroup(self().apply(doSomething()))
			.testEquals();
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() throws NoSuchMethodException {
		new NullPointerTester().testMethod(allDirectDescendants(), NodePredicate.class.getMethod("apply", ModelAction.class));
	}
}
