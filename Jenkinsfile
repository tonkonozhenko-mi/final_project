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
                input 'Deploy to Production?'
                sh '''
                    export IMAGE="$registry:$BUILD_NUMBER"
                    sed -ie "s~IMAGE~$IMAGE~g" kubernetes_front.yml
                    kubectl apply -f kubernetes_front.yaml
                    '''
            }
        }
    }
}
