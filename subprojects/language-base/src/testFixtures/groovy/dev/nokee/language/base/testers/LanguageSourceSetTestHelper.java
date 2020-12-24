//package dev.nokee.language.base.testers;
//
//import dev.nokee.language.base.LanguageSourceSet;
//import org.gradle.api.tasks.util.PatternFilterable;
//
//import java.util.function.Consumer;
//
//import static dev.nokee.internal.testing.utils.ClosureTestUtils.adaptToClosure;
//
//public final class LanguageSourceSetTestHelper {
//	public enum FilterMethods {
//		UsingAction {
//			@Override
//			@SuppressWarnings("unchecked")
//			public <T extends LanguageSourceSet> T call(T subject, Consumer<? super PatternFilterable> action) {
//				return (T) subject.filter(action::accept);
//			}
//		},
//		UsingClosure {
//			@Override
//			@SuppressWarnings("unchecked")
//			public <T extends LanguageSourceSet> T call(T subject, Consumer<? super PatternFilterable> action) {
//				return (T) subject.filter(adaptToClosure(action));
//			}
//		},
//		UsingGetter {
//			@Override
//			public <T extends LanguageSourceSet> T call(T subject, Consumer<? super PatternFilterable> action) {
//				action.accept(subject.getFilter());
//				return subject;
//			}
//		};
//
//		public abstract <T extends LanguageSourceSet> T call(T subject, Consumer<? super PatternFilterable> action);
//	}
//}
