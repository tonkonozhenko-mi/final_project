import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.dockerSupport
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dockerCommand
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
        password("env.AWS_ACCESS_KEY_ID", "credentialsJSON:a967de85-734d-4b88-91db-09db3c2a08c5", label = "access", description = "access", display = ParameterDisplay.HIDDEN, readOnly = true)
        param("ECR_IMAGE_NAME", "back-eschool-mt-%ENV%")
        param("ECR_NAME", "443172575185.dkr.ecr.us-east-2.amazonaws.com")
        param("docker.username", "AWS")
        param("ENV", "prod")
        param("docker.server", "https://%docker.registry%")
    }

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        script {
            name = "Changing environments"
            enabled = false
            scriptContent = """
                sed -i 's@https://fierce-shore-32592.herokuapp.com@http://3.131.64.51:8080@' /home/ubuntu/TeamCity_agent/work/df0b1de548f94fab/src/app/services/token-interceptor.service.ts 
                sed -i 's@http://@http://3.131.64.51:8080@' /home/ubuntu/TeamCity_agent/work/df0b1de548f94fab/src/environments/environment.ts
                
                sed -i 's@https://fierce-shore-32592.herokuapp.com@http://3.131.64.51:8080@' /home/ubuntu/teamcity/buildAgent/work/df0b1de548f94fab/src/app/services/token-interceptor.service.ts 
                sed -i 's@http://@http://3.131.64.51:8080@' /home/ubuntu/teamcity/buildAgent/work/df0b1de548f94fab/src/environments/environment.ts
            """.trimIndent()
        }
        script {
            name = "Yarn install"
            scriptContent = "yarn install"
        }
        script {
            name = "Build"
            scriptContent = "yarn run build"
        }
        step {
            name = """Upload build "dist""""
            type = "ssh-deploy-runner"
            enabled = false
            param("jetbrains.buildServer.deployer.username", "ubuntu")
            param("teamcitySshKey", "privat_aws.ppk")
            param("jetbrains.buildServer.deployer.sourcePath", "dist/eSchool => eSchool.zip")
            param("jetbrains.buildServer.deployer.targetUrl", "10.0.1.6:/var/www/html")
            param("jetbrains.buildServer.sshexec.authMethod", "UPLOADED_KEY")
            param("jetbrains.buildServer.deployer.ssh.transport", "jetbrains.buildServer.deployer.ssh.transport.scp")
        }
        step {
            name = """Unzip "dist""""
            type = "ssh-exec-runner"
            enabled = false
            param("jetbrains.buildServer.deployer.username", "ubuntu")
            param("jetbrains.buildServer.sshexec.command", """
                cd /var/www/html/
                cp -i eSchool.zip /var/www/eSchool.%env.BUILD_NUMBER%.zip
                unzip -o eSchool.zip
                rm eSchool.zip
                sudo systemctl restart apache2
            """.trimIndent())
            param("teamcitySshKey", "privat_aws.ppk")
            param("jetbrains.buildServer.deployer.targetUrl", "10.0.1.6")
            param("jetbrains.buildServer.sshexec.authMethod", "UPLOADED_KEY")
        }
        script {
            name = "Deploy to ECS"
            scriptContent = """
                ${'$'}(aws ecr get-login %AWS_REGION% --no-include-email)
                
                docker push %ECR_NAME%/%ECR_IMAGE_NAME%:%build.number%
                docker push %ECR_NAME%/%ECR_IMAGE_NAME%:latest
                
                esc deploy %ECS_CLUSTER_NAME% %ECS_SERVICE_NAME% %AWS_REGION% %ECS_DEPLOY_OPTIONS%
            """.trimIndent()
        }
        dockerCommand {
            name = "Build api"
            commandType = build {
                source = file {
                    path = "src/app"
                }
                namesAndTags = """
                    %ECR_NAME%/%ECR_IMAGE_NAME%:%build.number%
                    %ECR_NAME%/%ECR_IMAGE_NAME%:latest
                """.trimIndent()
                commandArgs = "--pull --build-arg env=%ENV%"
            }
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
