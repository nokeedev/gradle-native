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
package nokeebuild.buildscan;

import com.gradle.enterprise.gradleplugin.GradleEnterpriseExtension;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;

@SuppressWarnings("rawtypes")
final class ConfigureGradleEnterprisePlugin implements Action<Plugin> {
    private final Settings settings;
    private final Action<? super GradleEnterpriseExtension> action;

    public ConfigureGradleEnterprisePlugin(Settings settings, Action<? super GradleEnterpriseExtension> action) {
        this.settings = settings;
        this.action = action;
    }

    @Override
    public void execute(Plugin ignored) {
        if (!containsPropertiesTask(settings)) {
            gradleEnterprise(settings, action);
        }
    }

    // Disable build scan for security reason
    // https://github.com/gradle/gradle-enterprise-conventions-plugin/issues/9
    private static boolean containsPropertiesTask(Settings settings) {
        return settings.getGradle().getStartParameter().getTaskNames().contains("properties")
                || settings.getGradle().getStartParameter().getTaskNames().stream().anyMatch(it -> it.endsWith(":properties"));
    }

    private static void gradleEnterprise(Settings target, Action<? super GradleEnterpriseExtension> action) {
        action.execute(target.getExtensions().getByType(GradleEnterpriseExtension.class));
    }
}
