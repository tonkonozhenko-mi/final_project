pipeline {
    agent any
    environment {
        //be sure to replace "willbla" with your own Docker Hub username
        DOCKER_IMAGE_NAME_FRONT = "maxtonk/eschool-front"
    }
    stages {
        stage('checkout'){
            steps{
                git branch: 'master', url: 'https://github.com/tonkonozhenko-mi/final_project.git'
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    app = docker.build(DOCKER_IMAGE_NAME_FRONT)
                }
            }
        }
        stage('Push Docker Image') {
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', 'Docker_Hub') {
                        app.push("${env.BUILD_NUMBER}")
                        app.push("latest")
                    }
                }
            }
        }
        stage('Deploy To Kuber Cluster AWS') {
            steps {
                input 'Deploy to Production?'
                milestone(1)
                kubernetesDeploy(
                    kubeconfigId: 'kubeconfig_aws',
                    configs: 'kubernetes-front.yaml',
                    enableConfigSubstitution: true
                )
            }
        }
    }
}
