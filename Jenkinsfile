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
        stage('Run JAR') {
            steps {   
                sh 'pkill -f "/opt/battleship/test/*.jar" || true'
                sh 'JENKINS_NODE_COOKIE=dontKillMe nohup java -jar /opt/battleship/test/*.jar --server.port=8081 > /opt/battleship/test/app.log 2>&1 &'
            } //https://stackoverflow.com/questions/37341545/unable-to-run-nohup-command-from-jenkins-as-a-background-process test
        }
    }
}
