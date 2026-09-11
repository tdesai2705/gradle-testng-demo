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
    }
}
