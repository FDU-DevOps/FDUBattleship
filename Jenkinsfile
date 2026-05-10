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
        stage('Clean Releases Folder') {
            steps {
                sh 'rm -f /opt/battleship/test/releases/FDUBattleship-*.jar'
            }
        }
        stage('Copy JAR to Releases') {
            steps {
                sh 'cp target/FDUBattleship-*.jar /opt/battleship/test/releases/'
            }
        }
        // Just to see what is in the releases directory
        stage('Verify Releases Directory') {
            steps {
                sh 'ls -la /opt/battleship/test/releases/'
            }
        }
        stage('Trigger Deployment') {
            steps {
                sh 'touch /opt/battleship/test/releases/.deploy-trigger'
            }
        }
    }
}
