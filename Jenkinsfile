@Library('shared-pipeline-library') _

pipeline {
    agent any

    parameters {
        choice(
            name: 'ENVIRONMENT',
            choices: ['dev', 'staging', 'prod'],
            description: 'Target environment to run tests against'
        )
        choice(
            name: 'SUITE',
            choices: ['smoke', 'regression', 'full'],
            description: 'Test suite to execute'
        )
        booleanParam(
            name: 'HEADLESS',
            defaultValue: false,
            description: 'Run browser in headless mode'
        )
        booleanParam(
            name: 'PARALLEL',
            defaultValue: true,
            description: 'Run tests in parallel'
        )
    }

    environment {
        MAVEN_OPTS   = '-Xmx1024m -Xms512m'
        SUITE_XML    = "src/test/resources/suites/${params.SUITE}.xml"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Compile') {
            steps {
                script {
                    echo "Building for ENVIRONMENT=${params.ENVIRONMENT}"
                }
                bat 'mvn clean compile -q'
            }
        }

        stage('Run Tests') {
            steps {
                bat """
                    mvn test \
                        -Denv=${params.ENVIRONMENT} \
                        -DsuiteXmlFile=${SUITE_XML} \
                        -Ddriver.headless=${params.HEADLESS} \
                        -Dparallel=${params.PARALLEL} \
                        -DthreadCount=4 \
                        -Dmaven.test.failure.ignore=true
                """
            }
        }

        stage('Publish Reports') {
            steps {
                publishHTML([
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'reports/extent',
                    reportFiles: '*.html',
                    reportName: 'Extent Report'
                ])

                publishHTML([
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'reports/cucumber',
                    reportFiles: 'cucumber_report.html',
                    reportName: 'Cucumber Report'
                ])
            }
        }

        stage('Archive Artifacts') {
            steps {
                archiveArtifacts(
                    artifacts: 'reports/**/*.html, reports/**/*.xml, reports/**/*.json',
                    fingerprint: true,
                    allowEmptyArchive: true
                )

                archiveArtifacts(
                    artifacts: 'reports/test-output/screenshots/*.png',
                    fingerprint: true,
                    allowEmptyArchive: true
                )
            }
        }
    }

    post {
        always {
            cleanWs(
                cleanWhenNotBuilt: false,
                deleteDirs: true,
                notFailBuild: true
            )
        }
        failure {
            emailext(
                subject: "[${currentBuild.fullDisplayName}] Test Failure",
                body: "Automation tests failed on ${params.ENVIRONMENT}.\nSee: ${env.BUILD_URL}",
                to: '${emaillist}'
            )
        }
        unstable {
            emailext(
                subject: "[${currentBuild.fullDisplayName}] Test Unstable",
                body: "Automation tests are unstable on ${params.ENVIRONMENT}.",
                to: '${emaillist}'
            )
        }
    }
}
