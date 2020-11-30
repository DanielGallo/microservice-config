import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.finishBuildTrigger
import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2020.2"

object MicroserviceRepo : GitVcsRoot({
    name = DslContext.getParameter("vcsDisplayName")
    url = DslContext.getParameter("vcsUrl")
    branch = "refs/heads/main"
})

object Build : BuildType({
    name = "Build"

    triggers {
        vcs {
        }
    }

    vcs {
        root(MicroserviceRepo)
    }

    steps {
        script {
            name = "Run Build"
            scriptContent = """
                echo "Running Build!"
            """.trimIndent()
        }
    }
})

object Test : BuildType({
    name = "Test"

    steps {
        script {
            name = "Run Tests"
            scriptContent = """
                echo "Running Tests!"
            """.trimIndent()
        }
    }
})

object Deploy : BuildType({
    name = "Deploy"
    type = Type.DEPLOYMENT

    steps {
        script {
            name = "Run Deployment"
            scriptContent = """
                echo "Deploying!"
            """.trimIndent()
        }
    }
})

project {
    vcsRoot(MicroserviceRepo)

    buildType(Build)
    buildType(Test)

    if (DslContext.getParameter("deploy").equals("true")) {
        buildType(Deploy)

        sequential {
            buildType(Build)
            buildType(Test)
            buildType(Deploy)
        }
    } else {
        sequential {
            buildType(Build)
            buildType(Test)
        }
    }
}
