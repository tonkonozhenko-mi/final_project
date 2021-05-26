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




pipeline {
    agent any
    environment {
        registry = "maxtonk/eschool-front"
        registryCredential = 'Docker_Hub'
        dockerImage = ''
    }
    stages {
         stage('Building docker image') {
            steps{
                script {
                    dockerImage = docker.build registry + ":$BUILD_NUMBER"
                }
            }
        }
        stage('Push image to Docker Hub') {
            steps{
                script {
                    docker.withRegistry( '', registryCredential ) {
                        app.push("${env.BUILD_NUMBER}")
                        app.push("latest")
                    }
                }
            }
        }
        stage('Remove unused docker image') {
            steps{
                sh "docker rmi $registry:$BUILD_NUMBER"
            }
        }
        stage('Update kube config'){
            steps {
                withAWS(region:'us-west-2',credentials:'aws_creds') {
                    sh 'aws eks --region us-west-2 update-kubeconfig --name eks_cluster'                    
                }
            }
        }
        stage('Deploy updated image to AWS EKS'){
            steps {
                sh '''
                    export IMAGE="$registry:$BUILD_NUMBER"
                    sed -ie "s~IMAGE~$IMAGE~g" kubernetes_front.yml
                    kubectl apply -f kubernetes_front.yaml
                    '''
            }
        }
    }
}
