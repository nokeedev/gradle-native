package dev.nokee.model.internal.registry;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import dev.nokee.internal.testing.ClosureAssertions;
import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.ModelAction;
import dev.nokee.model.internal.core.ModelIdentifier;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelSpecs;
import dev.nokee.model.internal.type.ModelType;
import lombok.Data;
import lombok.val;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import spock.lang.Subject;

import java.util.function.Function;

import static dev.nokee.model.internal.core.ModelActions.executeUsingProjection;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.ModelNodes.withPath;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.ActionUtils.doNothing;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

@Subject(ModelNodeBackedProvider.class)
public class ModelNodeBackedProviderTest {
	private final ModelConfigurer modelConfigurer = Mockito.mock(ModelConfigurer.class);
	private final MyType myTypeInstance = new MyType();
	private final MyOtherType myOtherTypeInstance = new MyOtherType();
	private final ModelNode node = node("foo", builder -> builder.withConfigurer(modelConfigurer).withProjections(UnmanagedInstanceModelProjection.of(myTypeInstance), UnmanagedInstanceModelProjection.of(myOtherTypeInstance)));

	private <T> DomainObjectProvider<T> provider(Class<T> type) {
		return new ModelNodeBackedProvider<>(of(type), node);
	}

	@Test
	void throwExceptionWhenCreatingProviderWithWrongProjectionType() {
		val ex = assertThrows(IllegalArgumentException.class, () -> provider(WrongType.class));
		assertThat(ex.getMessage(),
			equalTo("node 'foo' cannot be viewed as interface dev.nokee.model.internal.registry.ModelNodeBackedProviderTest$WrongType"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester()
			.setDefault(ModelType.class, of(MyType.class))
			.setDefault(ModelNode.class, node)
			.testAllPublicConstructors(ModelNodeBackedProvider.class);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(provider(MyType.class), provider(MyType.class))
			.addEqualityGroup(provider(MyOtherType.class))
			.addEqualityGroup(new ModelNodeBackedProvider<>(of(MyType.class), node(ManagedModelProjection.of(MyType.class))))
			.testEquals();
	}

	@Nested
	class CanGet {
		@Test
		void canGetDefaultProjectionViaProvider() {
			assertThat(provider(MyType.class).get(), equalTo(myTypeInstance));
		}

		@Test
		void canGetSecondaryProjectionViaProvider() {
			assertThat(provider(MyOtherType.class).get(), equalTo(myOtherTypeInstance));
		}

		@Test
		void realizeNodeWhenProviderIsRealized() {
			provider(MyType.class).get();
			assertThat(node.getState(), equalTo(ModelNode.State.Realized));
		}
	}

	abstract class CanTransform {
		private final Provider<String> subject = transform(provider(MyType.class), MyType::getValue);

		protected abstract Provider<String> transform(DomainObjectProvider<MyType> provider, Function<MyType, String> mapper);

		@BeforeEach
		void configureDefaultValue() {
			myTypeInstance.setValue("default");
		}

		@Test
		void doesNotEagerlyRealizeModelNode() {
			assertThat(node.getState(), lessThan(ModelNode.State.Realized));
		}

		@Test
		void realizeModelNodeWhenMappedProviderIsQueried() {
			subject.getOrNull();
			assertThat(node.getState(), equalTo(ModelNode.State.Realized));
		}

		@Test
		void providerReturnsMappedValue() {
			assertThat(subject.get(), equalTo("default"));
		}

		@Test
		void changesToNodeProjectionAreReflectedByTheMappedProvider() {
			myTypeInstance.setValue("new-value");
			assertThat(subject.get(), equalTo("new-value"));
		}
	}

	@Nested
	class CanMap extends CanTransform {

		@Override
		protected Provider<String> transform(DomainObjectProvider<MyType> provider, Function<MyType, String> mapper) {
			return provider.map(mapper::apply);
		}
	}

	@Nested
	class CanFlatMap extends CanTransform {
		@Override
		protected Provider<String> transform(DomainObjectProvider<MyType> provider, Function<MyType, String> mapper) {
			return provider.flatMap(it -> TestUtils.providerFactory().provider(() -> mapper.apply(it)));
		}
	}

	@Test
	void canQueryProviderType() {
		assertThat(provider(MyType.class).getType(), equalTo(MyType.class));
		assertThat(provider(MyOtherType.class).getType(), equalTo(MyOtherType.class));
	}

	@Test
	void canQueryProviderIdentifier() {
		assertThat(provider(MyType.class).getIdentifier(), equalTo(ModelIdentifier.of("foo", MyType.class)));
		assertThat(provider(MyOtherType.class).getIdentifier(), equalTo(ModelIdentifier.of("foo", MyOtherType.class)));
	}

	@Test
	void checkToString() {
		assertThat(provider(MyType.class), hasToString("provider(node 'foo', class dev.nokee.model.internal.registry.ModelNodeBackedProviderTest$MyType)"));
		assertThat(provider(MyOtherType.class), hasToString("provider(node 'foo', class dev.nokee.model.internal.registry.ModelNodeBackedProviderTest$MyOtherType)"));
	}

	@Test
	void canConfigureProviderUsingAction() {
		provider(MyType.class).configure(doNothing());
		verify(modelConfigurer, times(1)).configureMatching(ModelSpecs.of(withPath(path("foo")).and(stateAtLeast(ModelNode.State.Realized))), executeUsingProjection(of(MyType.class), doNothing()));
	}

	@Test
	void canConfigureProviderUsingClosure() {
		doAnswer(invocation -> {
			invocation.getArgument(1, ModelAction.class).execute(node);
			return null;
		})
			.when(modelConfigurer)
			.configureMatching(eq(ModelSpecs.of(withPath(path("foo")).and(stateAtLeast(ModelNode.State.Realized)))), any());
		ClosureAssertions.executeWith(closure -> provider(MyType.class).configure(closure))
			.assertCalledOnce()
			.assertLastCalledArgumentThat(equalTo(myTypeInstance))
			.assertDelegateFirstResolveStrategy();
	}

	@Data
	static class MyType {
		private String value;
	}
	static class MyOtherType {}
	interface WrongType {}
}
