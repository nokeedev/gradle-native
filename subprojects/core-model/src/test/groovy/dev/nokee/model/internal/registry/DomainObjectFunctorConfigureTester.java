package dev.nokee.model.internal.registry;

import dev.nokee.internal.testing.ClosureAssertions;
import dev.nokee.model.internal.core.ModelAction;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelSpecs;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.model.internal.core.ModelActions.executeUsingProjection;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.ModelNodes.withPath;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.ActionUtils.doNothing;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public abstract class DomainObjectFunctorConfigureTester<F> extends AbstractDomainObjectFunctorTester<F> {
	private final ModelConfigurer modelConfigurer = Mockito.mock(ModelConfigurer.class);
	private final MyType myTypeInstance = new MyType();
	private final ModelNode node = node("foo", builder -> builder.withConfigurer(modelConfigurer).withProjections(UnmanagedInstanceModelProjection.of(myTypeInstance)));

	@Override
	protected <T> F createSubject(Class<T> type) {
		return createSubject(type, node);
	}

	protected void configure(F functor, Action<?> action) {
		invoke(functor, "configure", new Class[]{Action.class}, new Object[]{action});
	}

	@Test
	void canConfigureProviderUsingAction() {
		configure(createSubject(MyType.class), doNothing());
		verify(modelConfigurer, times(1)).configureMatching(ModelSpecs.of(withPath(path("foo")).and(stateAtLeast(ModelNode.State.Realized))), executeUsingProjection(of(MyType.class), doNothing()));
	}

	protected void configure(F functor, Closure<Void> closure) {
		invoke(functor, "configure", new Class[] {Closure.class}, new Object[] {closure});
	}

	@Test
	void canConfigureProviderUsingClosure() {
		doAnswer(invocation -> {
			invocation.getArgument(1, ModelAction.class).execute(node);
			return null;
		})
			.when(modelConfigurer)
			.configureMatching(eq(ModelSpecs.of(withPath(path("foo")).and(stateAtLeast(ModelNode.State.Realized)))), any());
		ClosureAssertions.executeWith(closure -> configure(createSubject(MyType.class), closure))
			.assertCalledOnce()
			.assertLastCalledArgumentThat(equalTo(myTypeInstance))
			.assertDelegateFirstResolveStrategy();
	}
}
