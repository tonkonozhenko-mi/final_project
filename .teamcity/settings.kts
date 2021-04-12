import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.dockerSupport
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs

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

project {
    description = "FrontEnd"

    buildType(Build)
}

object Build : BuildType({
    name = "Build"

    artifactRules = "dist/eSchool => eSchool"

    params {
        param("AWS_REGION", "--region us-east-2")
        param("ECS_CLUSTER_NAME", "back-eschool-mt")
        password("env.AWS_SECRET_ACCESS_KEY", "credentialsJSON:c40ecf2b-1f3b-4e13-b101-620baccc2b2c", label = "key", description = "key", display = ParameterDisplay.HIDDEN, readOnly = true)
        param("ECS_SERVICE_NAME", "api-%ENV%")
        param("ECS_DEPLOY_OPTIONS", "--ignore-warnings --timeout 500")
        param("ECR_IMAGE_NAME", "back-eschool-mt-%ENV%")
        param("ECR_NAME", "443172575185.dkr.ecr.us-east-2.amazonaws.com")
        param("docker.registry", "443172575185.dkr.ecr.us-east-2.amazonaws.com")
        param("docker.username", "AWS")
        param("ENV", "prod")
        param("docker.server", "https://%docker.registry%")
    }

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        script {
            name = "docker login"
            scriptContent = "aws ecr get-login-password --region us-east-2 | docker login --username AWS --password-stdin 443172575185.dkr.ecr.us-east-2.amazonaws.com"
        }
        script {
            name = "docker build"
            scriptContent = "docker build -t front-eschool-mt ."
        }
        script {
            name = "docker tag"
            scriptContent = "docker tag front-eschool-mt:latest 443172575185.dkr.ecr.us-east-2.amazonaws.com/front-eschool-mt:latest"
        }
        script {
            name = "docker push"
            scriptContent = "docker push 443172575185.dkr.ecr.us-east-2.amazonaws.com/front-eschool-mt:latest"
        }
        script {
            name = "docker logout"
            scriptContent = "docker logout %docker.server%"
        }
    }

    triggers {
        vcs {
        }
    }

    features {
        dockerSupport {
        }
    }
})
