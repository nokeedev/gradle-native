package dev.nokee.model.dsl;

import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.TestProjection;
import groovy.lang.Closure;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.function.Function;

import static dev.nokee.internal.testing.ExecuteWith.*;
import static dev.nokee.internal.testing.utils.ClosureTestUtils.adaptToClosure;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertAll;

public interface ModelNodeTester {
	ModelNode createSubject();

//	@Test
//	default void canCreateNode() {
//		assertAll(
//			() -> assertThat(createSubject().node("test"), isA(ModelNode.class)),
//			() -> assertThat(createSubject().node("test", ActionUtils.doNothing()), isA(ModelNode.class)),
//			() -> assertThat(createSubject().node("test", adaptToClosure(it -> {})), isA(ModelNode.class)),
//			() -> assertThat(createSubject().node("test", TestProjection.class), isA(KnownDomainObject.class))
//		);
//	}
//
//	@Test
//	default void canCreateAndConfigureNode() {
//		assertAll(
//			() -> assertThat(executeWith(closure(c -> createSubject().node("test", c))),
//				calledOnceWith(isA(ModelNode.class))),
//			() -> assertThat(executeWith(action(a -> createSubject().node("test", a))),
//				calledOnceWith(isA(ModelNode.class)))
//		);
//	}

//	@Test
//	default void canCreateNodeAndProjectAndConfigureNode() {
//		assertAll(
//			() -> assertThat(executeWith(closure(c -> createSubject().node("test", TestProjection.class, c))),
//				calledOnceWith(isA(ModelNode.class))),
//			() -> assertThat(executeWith(biConsumer(a -> createSubject().node("test", TestProjection.class, a))),
//				calledOnceWith(firstArgumentOf(isA(ModelNode.class))))
//		);
//	}

//	@Test
//	default void returnsExistingNodeIfExists() {
//		assertAll(
//			assertCanGetExistingNode(this, subject -> subject.node("existing")),
//			assertCanGetExistingNode(this, subject -> subject.node("existing", ActionUtils.doNothing())),
//			assertCanGetExistingNode(this, subject -> subject.node("existing", adaptToClosure(it -> {})))
//		);
//	}
//	static Executable assertCanGetExistingNode(ModelNodeTester self, UnaryOperator<ModelNode> createNode) {
//		return () -> {
//			val subject = self.createSubject();
//			val existingNode = subject.node("existing");
//			assertThat(createNode.apply(subject), equalTo(existingNode));
//		};
//	}

//	@Test
//	default void canReturnExistingProjectionIfExists() {
//		assertAll(
//			assertCanGetExistingNodeProjection(this, subject -> subject.node("test", TestProjection.class)),
//			assertCanGetExistingNodeProjection(this, subject -> subject.node("test", TestProjection.class, (a, b) -> {})),
//			assertCanGetExistingNodeProjection(this, subject -> subject.node("test", TestProjection.class, Closure.IDENTITY)),
//			assertCanGetExistingNodeProjection(this, subject -> subject.node("test").projection(TestProjection.class))
//		);
//	}
//	static Executable assertCanGetExistingNodeProjection(ModelNodeTester self, Function<? super ModelNode, ? extends KnownDomainObject<TestProjection>> createProjection) {
//		return () -> {
//			val subject = self.createSubject();
//			val existingDomainObject = subject.node("existing").projection(TestProjection.class);
//			assertThat(createProjection.apply(subject), equalTo(existingDomainObject));
//		};
//	}

//	@Test
//	default void canCallbackWithModelNodeOnly() {
//		assertThat(executeWith(closure(c -> createSubject().node("test", TestProjection.class, c))),
//			calledOnceWith(isA(ModelNode.class)));
//	}

//	@Test
//	default void canCallbackWithModelNodeAndProjection() {
//		assertAll(
//			() -> {
//				val result = executeWith(biClosure(c -> createSubject().node("test", TestProjection.class, c)));
//				assertThat(result, calledOnceWith(firstArgumentOf(isA(ModelNode.class))));
//				assertThat(result, calledOnceWith(secondArgumentOf(isA(KnownDomainObject.class))));
//			},
//			() -> {
//				val result = executeWith(biConsumer(c -> createSubject().node("test", TestProjection.class, c)));
//				assertThat(result, calledOnceWith(firstArgumentOf(isA(ModelNode.class))));
//				assertThat(result, calledOnceWith(secondArgumentOf(isA(KnownDomainObject.class))));
//			}
//		);
//	}

	// TODO: Create projection for domain object type that already exists will reuse same projection

//	@Test
//	default void registersProjection() {
//		createSubject().projection(TestProjection.class)
//	}
}
