package dev.nokee.model.internal.type;

import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

class ModelType_InterfacesTest {
	@Test
	void canGetInterfacesForClassImplementingNoInterface() {
		assertThat(of(Base.class).getInterfaces(), empty());
	}

	@Test
	void canGetInterfacesForInterfaceExtendingNoInterface() {
		assertThat(of(Iface.class).getInterfaces(), empty());
	}

	static class Base {}
	interface Iface {}

	@Test
	void canGetInterfacesForInterfaceExtendingOneInterface() {
		assertThat(of(I1.class).getInterfaces(), contains(of(Iface.class)));
	}

	interface I1 extends Iface {}

	@Test
	void canGetInterfacesForInterfaceExtendingMultipleInterface() {
		assertThat(of(I2.class).getInterfaces(), contains(of(Iface1.class), of(Iface2.class)));
	}

	interface Iface1 {}
	interface Iface2 {}
	interface I2 extends Iface1, Iface2 {}

	@Test
	void canGetInterfacesForClassImplementingOneInterface() {
		assertThat(of(C1.class).getInterfaces(), contains(of(Iface.class)));
	}

	static class C1 implements Iface {}

	@Test
	void canGetInterfacesForClassImplementingMultipleInterface() {
		assertThat(of(C2.class).getInterfaces(), contains(of(Iface1.class), of(Iface2.class)));
	}

	static class C2 implements Iface1, Iface2 {}


	@Test
	void canGetGenericInterfacesForConcreteInterface() {
		assertThat(of(IMyStringList.class).getInterfaces(), contains(of(new TypeOf<IMyList<String>>() {})));
	}

	@Test
	void canGetGenericInterfacesForParameterizedInterface() {
		assertThat(of(new TypeOf<IMyList<Integer>>() {}).getInterfaces(), contains(of(new TypeOf<IMyCollection<Integer>>() {})));
	}

	interface IMyCollection<T> {}
	interface IMyList<T> extends IMyCollection<T> {}
	interface IMyStringList extends IMyList<String> {}

	@Test
	void canGetGenericInterfacesForConcreteClass() {
		assertThat(of(MyStringList.class).getInterfaces(), contains(of(new TypeOf<IMyList<String>>() {})));
	}

	@Test
	void canGetGenericInterfacesForParameterizedClass() {
		assertThat(of(new TypeOf<MyList<Integer>>() {}).getInterfaces(), contains(of(new TypeOf<IMyCollection<Integer>>() {})));
	}

	static class MyList<T> implements IMyCollection<T> {}
	static class MyStringList implements IMyList<String> {}
}
