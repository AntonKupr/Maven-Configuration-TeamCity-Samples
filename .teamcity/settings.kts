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

    // Build Configuration
    buildType {
        id("BuildAndTest")
        name = "Build and Test"
        description = "Builds and tests the Maven Simple Project"

        // VCS Settings
        vcs {
            root(mainVcsRoot)
        }

        // Build Steps
        steps {
            // Compile and test the project
            maven {
                name = "Compile and Test"
                goals = "clean test"
                runnerArgs = "-Dmaven.test.failure.ignore=true"
                mavenVersion = defaultProvidedVersion()
                userSettingsSelection = "settings.xml"
                jdkHome = "%env.JDK_11%"
            }

            // Package the application
            maven {
                name = "Package"
                goals = "package"
                mavenVersion = defaultProvidedVersion()
                userSettingsSelection = "settings.xml"
                jdkHome = "%env.JDK_11%"
                workingDir = "ch-simple"
            }
        }

        // Triggers
        triggers {
            vcs {
                branchFilter = "+:*"
            }
        }

        // Build Features
        features {
            perfmon {
            }
        }

        // Artifact Rules
        artifactRules = """
            ch-simple/simple/target/*.jar => simple-artifacts
        """.trimIndent()
    }

    // Deployment Configuration
    buildType {
        id("DeployApplication")
        name = "Deploy Application"
        description = "Deploys the Maven Simple Project"

        // VCS Settings
        vcs {
            root(mainVcsRoot)
        }

        // Build Steps
        steps {
            script {
                name = "Deploy to Environment"
                scriptContent = """
                    echo "Deploying application to environment..."
                    mkdir -p deploy
                    cp ch-simple/simple/target/*.jar deploy/
                    echo "Deployment completed successfully!"
                """.trimIndent()
            }
        }

        // Dependencies
        dependencies {
            snapshot(RelativeId("BuildAndTest")) {
                reuseBuilds = ReuseBuilds.ANY
                onDependencyFailure = FailureAction.FAIL_TO_START
            }
        }

        // Build Features
        features {
            perfmon {
            }
        }
    }
}
