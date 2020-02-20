#!groovy
pipeline {

    agent any
    environment {
        mvnHome = tool 'maven'
    }
    
    stages {
        stage('Build') {
            steps {
                echo 'Build'
                sh "'${mvnHome}/bin/mvn' clean package -DskipTests"
            }
        }
    
    
        stage('Test') {
            steps {
                echo 'Skip Tests'
                //sh "'${mvnHome}/bin/mvn' test"
            }
        }
        
        stage('Deliver') { 
            steps {
                sh './deliver.sh' 
            }
        }
    }
}
