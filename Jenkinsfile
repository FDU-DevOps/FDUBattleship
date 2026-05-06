// Initialize application version to empty at the global level
def globalAppVersion = "unknown"

pipeline {

    agent any

    stages {

        stage('Verify Environment') {
            steps {
                echo "Running manual retrieval for branch: ${env.GIT_BRANCH}"
                sh 'pwd'
                sh 'whoami'
            }
        }

        stage('Checkout Branch') {
            steps {
                // Ensure a full clone so git tags are visible for versioning
                checkout([$class: 'GitSCM',
                    branches: [[name: "${env.GIT_BRANCH}"]],
                    userRemoteConfigs: [[url: 'https://github.com/FDU-DevOps/FDUBattleship.git']],
                    extensions: [[$class: 'CloneOption', shallow: false, noTags: false, depth: 0]]
                ])
            }
        }

        stage('Verify Retrieved Files') {
            steps {
                sh 'ls -la'
            }
        }

        stage('Build JAR') {
            steps {
                script {def 
                    branchName = env.BRANCH_NAME ?: sh(script: "git rev-parse --abbrev-ref HEAD", returnStdout: true).trim()


                    if (branchName == 'master' || branchName == 'main') {
                        // 1. Get the latest Git Tag
                        try {
                            globalAppVersion = sh(script: "git describe --tags --abbrev=0", returnStdout: true).trim()
                            // Strip the 'v' if your tags look like v1.0.0
                            globalAppVersion = globalAppVersion.replace('v', '')
                        } catch (Exception e) {
                            // Fallback if no tag exists yet
                            globalAppVersion = "1.0.0-${env.BUILD_NUMBER}"
                        }
                    } else {
                        // 2. Format for branches (e.g., feature-logic-b42)
                        def safeBranchName = branchName.replaceAll("/", "-")
                        globalAppVersion = "${safeBranchName}-b${env.BUILD_NUMBER}"
                    }

                    echo "--- Building Wordle App Version: ${globalAppVersion} ---"

                    // 3. Run the build and INJECT the version
                    withMaven(maven: 'Maven') {
                        sh "mvn clean package -Dmaven.compiler.release=21 -Dproject.version=${globalAppVersion}"
                    }
                }
            }
        }

        stage('Copy to Test Directory') {
            steps {
                sh 'mkdir -p /opt/battleship/test'
                sh 'rm -rf /opt/battleship/test/*'
                sh 'cp target/FDUBattleship-*.jar /opt/battleship/test/'
            }
        }

        stage('Verify Deployment Directory') {
            steps {
                sh 'ls -la /opt/battleship/test'
            }
        }

        stage('Trigger Service') {
            steps {
                sh 'touch /opt/battleship/test/.restart-trigger'
            }
        }

    }
}
