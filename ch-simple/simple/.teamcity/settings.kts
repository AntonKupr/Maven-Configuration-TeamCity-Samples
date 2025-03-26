import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in IntelliJ IDEA, open the 'TeamCity'
tool window (View -> Tool Windows -> TeamCity), then click the
'Debug' button in the toolbar and select the desired settings file.
*/

version = "2019.2"

project {
    description = "Simple Chapter Project Example"

    // Define Main VCS Root
    val mainVcsRoot = DslContext.settingsRoot

    // Build Configuration
    buildType {
        id("Build")
        name = "Build"

        vcs {
            root(mainVcsRoot)
        }

        steps {
            maven {
                name = "Clean and Package"
                goals = "clean package"
                runnerArgs = "-Dmaven.test.failure.ignore=true"
                userSettingsSelection = "settings.xml"
            }
        }

        triggers {
            vcs {
                branchFilter = "+:*"
            }
        }

        features {
            perfmon {
            }
        }
    }

    // Test Configuration
    buildType {
        id("Test")
        name = "Test"

        vcs {
            root(mainVcsRoot)
        }

        steps {
            maven {
                name = "Run Tests"
                goals = "test"
                runnerArgs = "-Dmaven.test.failure.ignore=true"
                userSettingsSelection = "settings.xml"
            }
        }

        triggers {
            vcs {
                branchFilter = "+:*"
            }
        }

        features {
            perfmon {
            }
        }
    }

    // Deploy Configuration
    buildType {
        id("Deploy")
        name = "Deploy"

        vcs {
            root(mainVcsRoot)
        }

        steps {
            maven {
                name = "Deploy to Repository"
                goals = "deploy"
                runnerArgs = "-DskipTests"
                userSettingsSelection = "settings.xml"
            }

            script {
                name = "Deployment Notification"
                scriptContent = """
                    echo "Deployment completed successfully!"
                    echo "Artifact: simple-0.8-SNAPSHOT.jar"
                """.trimIndent()
            }
        }

        dependencies {
            snapshot(RelativeId("Build")) {
                onDependencyFailure = FailureAction.FAIL_TO_START
            }
            snapshot(RelativeId("Test")) {
                onDependencyFailure = FailureAction.FAIL_TO_START
            }
        }

        features {
            perfmon {
            }
        }
    }
}
