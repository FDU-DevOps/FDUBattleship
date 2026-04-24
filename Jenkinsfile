pipeline {
    agent any
    stages {
        stage('Verify Environment') {
            steps {
                echo "Running retrieval for push (for test)"
                sh 'pwd'
                sh 'whoami'
            }
        }
        stage('Verify Retrieved Files') {
            steps {
                sh 'ls -la'
            }
        }
        stage('Build JAR') {
            steps {
                withMaven(maven: 'Maven') {   //using plugin for maven pipeline
                  sh 'mvn clean package -Dmaven.compiler.release=21'
                }
            }
        }
        stage('Copy to Test Directory') {
            steps {
                sh 'mkdir -p /opt/battleship/test'
                sh 'rm -rf /opt/battleship/test/*'
                sh 'cp target/*.jar /opt/battleship/test/'
            }
        }
        stage('Verify Deployment Directory') {
            steps {
                sh 'ls -la /opt/battleship/test'
            }
        }
       stage('Trigger Service') {
            steps {
            // signal systemd to restart the app
                sh 'touch /opt/battleship/test/.restart-trigger'
            }
        }
    }
}
