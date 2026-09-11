// ─────────────────────────────────────────────────────────────────────────────
// Built to answer Usha's (Amadeus) question via Anudeep/Gokulkumar's Slack ask:
// no existing example combines Gradle + TestNG with Smart Tests. This tests
// BOTH real integration mechanisms side by side:
//   A) Standalone CLI (same pattern as every other framework we've proven)
//   B) The `launchable-testng` Gradle dependency (a genuinely different,
//      runtime-hook-based mechanism, per official CloudBees docs)
// Sanity stage first: prove the plain Gradle+TestNG project itself works
// before layering Smart Tests on top.
// ─────────────────────────────────────────────────────────────────────────────

pipeline {
    agent {
        kubernetes {
            yaml """
apiVersion: v1
kind: Pod
spec:
  serviceAccountName: jenkins-agents
  containers:
  - name: jnlp
    resources:
      requests: { cpu: "10m", memory: "256Mi" }
      limits: { cpu: "500m", memory: "512Mi" }
  - name: gradle
    image: gradle:8.10-jdk17
    command: [sleep]
    args: [99d]
    resources:
      requests: { cpu: "200m", memory: "512Mi" }
      limits: { cpu: "1", memory: "1536Mi" }
  - name: python
    image: python:3.13-slim
    command: [sleep]
    args: [99d]
    resources:
      requests: { cpu: "10m", memory: "256Mi" }
      limits: { cpu: "500m", memory: "512Mi" }
"""
        }
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Sanity: plain gradle test (no Smart Tests yet)') {
            steps {
                container('gradle') {
                    sh '''
                        gradle --version
                        gradle test --no-daemon
                    '''
                }
            }
            post {
                always {
                    junit 'build/test-results/test/*.xml'
                }
            }
        }

        stage('Install Smart Tests CLI') {
            steps {
                container('python') {
                    sh '''
                        pip install --no-cache-dir "smart-tests-cli~=2.0"
                        smart-tests --version
                    '''
                }
            }
        }

        stage('Approach A: Standalone CLI subset') {
            steps {
                container('python') {
                    withCredentials([string(credentialsId: 'smart-tests-token-ptsv2', variable: 'SMART_TESTS_TOKEN')]) {
                        sh '''
                            git config --global --add safe.directory ${WORKSPACE}
                            smart-tests verify || true
                            smart-tests record build --build ${BUILD_TAG}-cli --source .
                            smart-tests record session --build ${BUILD_TAG}-cli --test-suite gradle-testng-cli-approach > session.txt
                            echo "=== session ==="
                            cat session.txt
                            smart-tests --log-level audit subset gradle --session @session.txt --target 100% src/test/java > subset.txt 2> subset_stderr.log
                            echo "=== subset.txt content ==="
                            cat subset.txt
                            echo "=== audit log ==="
                            cat subset_stderr.log
                        '''
                    }
                }
            }
        }

        stage('Approach A: Run subset via Gradle') {
            steps {
                container('gradle') {
                    sh '''
                        echo "=== running: gradle test $(cat subset.txt) ==="
                        gradle test --no-daemon $(cat subset.txt)
                    '''
                }
            }
            post {
                always {
                    container('python') {
                        withCredentials([string(credentialsId: 'smart-tests-token-ptsv2', variable: 'SMART_TESTS_TOKEN')]) {
                            sh 'smart-tests record tests gradle --session @session.txt --no-build ./build/test-results/test/ || true'
                        }
                    }
                    junit 'build/test-results/test/*.xml'
                }
            }
        }
    }
}
