package dev.nokee.language

import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.language.java.JavaTaskNames

trait MixedLanguageTaskNames implements LanguageTaskNames {
    LanguageTaskNamesRegistry getTaskNames() {
        return new LanguageTaskNamesRegistry()
    }

    static class LanguageTaskNamesRegistry {
        JavaTaskNames getJava() {
            return [] as JavaTaskNames
        }

        CppTaskNames getCpp() {
            return [] as CppTaskNames
        }
    }

//    /**
//     * Returns the tasks for the root project.
//     */
//    ProjectTasks getTasks() {
//        return new ProjectTasks(''/*, toolchainUnderTest, languageTaskSuffix, additionalTestTaskNames*/)
//    }

//    static class ProjectTasks {
//        private final String project
//
//        ProjectTasks(String project) {
//            this.project = project
//        }
//    }
}
