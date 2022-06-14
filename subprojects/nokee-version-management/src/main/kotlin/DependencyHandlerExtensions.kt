import dev.nokee.nvm.NokeeVersionManagementDependencyExtension
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.ExtensionAware

fun DependencyHandler.nokeeApi(): Dependency = ExtensionAware::class.java.cast(this).extensions.getByType(
	NokeeVersionManagementDependencyExtension::class.java).nokeeApi()
