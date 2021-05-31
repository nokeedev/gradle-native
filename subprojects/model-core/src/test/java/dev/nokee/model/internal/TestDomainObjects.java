package dev.nokee.model.internal;

import lombok.Data;
import org.gradle.api.Named;

import javax.inject.Inject;

public class TestDomainObjects {
	interface Bean extends Named {
		String getBeanProperty();
		void setBeanProperty(String value);
	}

	@Data
	static abstract class AbstractBean implements Bean {
		public final String name;
		String beanProperty;

		public AbstractBean(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	static final class DefaultBean extends AbstractBean {
		@Inject
		public DefaultBean(String name) {
			super(name);
		}
	}

	static class BeanSub1 extends AbstractBean {
		public BeanSub1(String name) {
			super(name);
		}
	}

	static class BeanSub2 extends AbstractBean {
		public BeanSub2(String name) {
			super(name);
		}
	}
}
