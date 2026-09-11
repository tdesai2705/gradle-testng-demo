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
                        apt-get update -qq
                        apt-get install -y --no-install-recommends git default-jre-headless >/dev/null
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
                            sh 'smart-tests record tests gradle --session @session.txt ./build/test-results/test/*.xml'
                        }
                    }
                    junit 'build/test-results/test/*.xml'
                }
            }
        }

        stage('Approach B: launchable-testng plugin subset') {
            steps {
                container('python') {
                    withCredentials([string(credentialsId: 'smart-tests-token-ptsv2', variable: 'SMART_TESTS_TOKEN')]) {
                        sh '''
                            smart-tests record build --build ${BUILD_TAG}-plugin --source .
                            smart-tests record session --build ${BUILD_TAG}-plugin --test-suite gradle-testng-plugin-approach > session-b.txt
                            echo "=== session (approach B) ==="
                            cat session-b.txt
                            smart-tests --log-level audit subset gradle --session @session-b.txt --target 40% --bare src/test/java > subset-b.txt 2> subset_b_stderr.log
                            echo "=== subset-b.txt content (bare class names) ==="
                            cat subset-b.txt
                            echo "=== audit log ==="
                            cat subset_b_stderr.log
                        '''
                    }
                }
            }
        }

        stage('Approach B: Run via launchable-testng plugin (SMART_TESTS_ env var)') {
            steps {
                container('gradle') {
                    sh '''
                        rm -rf build/test-results build/reports
                        export SMART_TESTS_SUBSET_FILE_PATH=$PWD/subset-b.txt
                        echo "=== Env var set: SMART_TESTS_SUBSET_FILE_PATH=$SMART_TESTS_SUBSET_FILE_PATH ==="
                        cat subset-b.txt
                        gradle test --no-daemon --rerun-tasks
                    '''
                }
            }
            post {
                always {
                    sh 'echo "=== Tests actually executed (Approach B, new env var) ===" && ls build/test-results/test/*.xml 2>/dev/null | wc -l'
                    junit 'build/test-results/test/*.xml'
                }
            }
        }
    }
}
