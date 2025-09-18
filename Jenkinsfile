pipeline {
    agent any

    environment {
        REGISTRY = "ghcr.io"
        VERSION = "1.0"
    }

    stages {
        stage('Clone Repository') {
            steps {
                checkout scm
            }
        }

        stage('Maven Test & Package') {
            steps {
                script {
                    def services = [
                        "mathengine-core",
                        "mathengine-assist",
                        "mathengine-client",
                        "mathengine-gateway",
                        "mathengine-service-registry",
                        "mathengine-admin"
                    ]

                    services.each { service ->
                        dir(service) {
                            echo "Running tests and packaging for ${service}"
                            bat "./mvnw clean package -DskipTests=false"
                        }
                    }
                }
            }
        }

        stage('Build & Push Images') {
            steps {
                script {
                    def services = [
                        "mathengine-core",
                        "mathengine-assist",
                        "mathengine-client",
                        "mathengine-gateway",
                        "mathengine-service-registry",
                        "mathengine-admin"
                    ]

                    withCredentials([usernamePassword(credentialsId: 'ghcr-token', usernameVariable: 'GH_USER', passwordVariable: 'GH_TOKEN')]) {
                        bat "echo $GH_TOKEN | docker login ${REGISTRY} -u $GH_USER --password-stdin"

                        services.each { service ->
                            def imageName = service.toLowerCase()
                            echo "Building image for ${service}"
                            bat "docker build -t ${REGISTRY}/${GH_USER}/${imageName}:${VERSION} ."
                            bat "docker push ${REGISTRY}/${GH_USER}/${imageName}:${VERSION}"
                        }
                    }
                }
            }
        }
    }
}
