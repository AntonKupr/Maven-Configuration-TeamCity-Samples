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
    description = "Maven Simple Project"

    // Define Main VCS Root
    val mainVcsRoot = DslContext.settingsRoot

    // Build Configuration for building and testing the app
    buildType {
        id("Build")
        name = "Build and Test"
        description = "Builds and tests the Maven Simple Project"

        vcs {
            root(mainVcsRoot)
        }

        steps {
            maven {
                name = "Build and Test"
                goals = "clean test"
                runnerArgs = "-Dmaven.test.failure.ignore=true"
                mavenVersion = auto()
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

    // Build Configuration for deploying the app
    buildType {
        id("Deploy")
        name = "Deploy"
        description = "Deploys the Maven Simple Project"

        vcs {
            root(mainVcsRoot)
        }

        steps {
            maven {
                name = "Package"
                goals = "clean package"
                mavenVersion = auto()
            }
            
            script {
                name = "Deploy"
                scriptContent = """
                    echo "Deploying the application..."
                    echo "Deployment completed successfully!"
                """.trimIndent()
            }
        }

        dependencies {
            snapshot(RelativeId("Build")) {
                onDependencyFailure = FailureAction.FAIL_TO_START
            }
        }

        features {
            perfmon {
            }
        }
    }
}