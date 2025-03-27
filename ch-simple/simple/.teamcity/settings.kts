import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

version = "2019.2"

project {
    description = "Simple Chapter Project Example"

    // Define Main VCS Root
    val mainVcsRoot = DslContext.settingsRoot

    // Build Configuration
    buildType(Build)

    // Deploy Configuration
    buildType(Deploy)
}

// Build Configuration
object Build : BuildType({
    name = "Build"
    description = "Builds and tests the project"

    // VCS Settings
    vcs {
        root(DslContext.settingsRoot)
    }

    // Build Steps
    steps {
        maven {
            name = "Clean and Package"
            goals = "clean package"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
            userSettingsSelection = "settings.xml"
        }
    }

    // Triggers
    triggers {
        vcs {
            branchFilter = "+:*"
        }
    }

    // Features
    features {
        perfmon {
        }
    }

    // Artifact Rules
    artifactRules = """
        target/*.jar
    """.trimIndent()
})

// Deploy Configuration
object Deploy : BuildType({
    name = "Deploy"
    description = "Deploys the application"

    // VCS Settings
    vcs {
        root(DslContext.settingsRoot)
    }

    // Build Steps
    steps {
        script {
            name = "Deploy Application"
            scriptContent = """
                echo "Deploying application..."
                mkdir -p deploy
                cp target/*.jar deploy/
                echo "Application deployed successfully!"
            """.trimIndent()
        }
    }

    // Dependencies
    dependencies {
        snapshot(Build) {
            reuseBuilds = ReuseBuilds.SUCCESSFUL
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }

    // Features
    features {
        perfmon {
        }
    }
})
