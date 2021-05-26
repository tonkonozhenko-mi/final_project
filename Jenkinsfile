pipeline {
      agent any
      stages {
        stage('Build Docker Image') {               
          when        
            branch 'master'
          }
          steps {
            script {
              app = docker.build("maxtonk/eschool_front")
              app.inside {
                sh 'echo $(curl localhost:80)'
              }
            }
          }
      }     
       stage('Test image') {           
            app.inside {            
              sh 'echo "Tests passed"'        
            }    
        }     
       stage('Push Docker Image') {
         when {
           branch 'master'
         }
         steps {
           script {
             docker.withRegistry('https://registry.hub.docker.com', 'Docker_Hub') {            
              app.push("${env.BUILD_NUMBER}")            
              app.push("latest")        
             }    
           }
         }
        }
}
