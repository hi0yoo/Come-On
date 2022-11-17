# Come On 프로젝트 Jenkins 파이프라인 구성

## Pipeline Job : come-on-service
Github Webhook으로부터 Github Repository에 변경사항 발생을 전달받고, 하위 Job으로 태스크를 수행시킵니다.
각 마이크로 서비스의 모든 하위 Job 수행이 완료되면 ssh를 통해 원격 서버에 새로운 도커 이미지를 내려받고 실행하도록 명령합니다.

```groovy
def BUILDX_BUILDER = ''

pipeline {
    agent any
    environment {
        TARGET_BRANCH = 'develop'

        SLACK_CHANNEL = '#be-jenkins'
        TEAM_DOMAIN = 'the-come-on'
        SLACK_TOKEN_ID = 'slack'
    }

    stages {

        stage('start') {
            steps {
                slackSend(
                        channel: SLACK_CHANNEL,
                        color: '#FFFF00',
                        message: "STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})",
                        teamDomain: TEAM_DOMAIN,
                        tokenCredentialId: SLACK_TOKEN_ID
                )
            }
        }

        stage('github clone & init') {
            when {
                environment(name: "BRANCH", value: TARGET_BRANCH)
            }
            steps {
                echo 'git clone...'
                git branch: TARGET_BRANCH, credentialsId: 'repo-and-hook-access-token-credentials', url: 'https://github.com/YooHayoung/Come-On.git'

                script {
                    BUILDX_BUILDER = sh(
                            script: 'docker buildx create --use',
                            returnStdout: true
                    ).trim()
                }
            }
        }

        stage('auth-service') {
            when {
                not {
                    environment(name: "AUTH_UPDATED", value: "[]")
                }
            }
            steps {
                dir('auth-service') {
                    script {
                        PROJECT_VERSION = sh(
                                script: './gradlew properties | grep -e ^version: | cut -d ":" -f 2 | sed -e "s/^ *//g" -e "s/ *$//g"',
                                returnStdout: true
                        ).trim()
                    }

                    build job: 'come-on-service-dev',
                            parameters: [
                                    string(name: 'project_name', value: 'auth-service'),
                                    string(name: 'project_version', value: PROJECT_VERSION),
                                    booleanParam(name: 'test_flag', value: true)
                            ]
                }
            }
        }

        stage('meeting-service') {
            when {
                not {
                    environment(name: "MEETING_UPDATED", value: "[]")
                }
            }
            steps {
                dir('meeting-service') {
                    script {
                        PROJECT_VERSION = sh(
                                script: './gradlew properties | grep -e ^version: | cut -d ":" -f 2 | sed -e "s/^ *//g" -e "s/ *$//g"',
                                returnStdout: true
                        ).trim()
                    }

                    build job: 'come-on-service-dev',
                            parameters: [
                                    string(name: 'project_name', value: 'meeting-service'),
                                    string(name: 'project_version', value: PROJECT_VERSION),
                                    booleanParam(name: 'test_flag', value: true)
                            ]
                }
            }
        }

        stage('course-service') {
            when {
                not {
                    environment(name: "COURSE_UPDATED", value: "[]")
                }
            }
            steps {
                dir('course-service') {
                    script {
                        PROJECT_VERSION = sh(
                                script: './gradlew properties | grep -e ^version: | cut -d ":" -f 2 | sed -e "s/^ *//g" -e "s/ *$//g"',
                                returnStdout: true
                        ).trim()
                    }

                    build job: 'come-on-service-dev',
                            parameters: [
                                    string(name: 'project_name', value: 'course-service'),
                                    string(name: 'project_version', value: PROJECT_VERSION),
                                    booleanParam(name: 'test_flag', value: true)
                            ]
                }
            }
        }

        stage('user-service') {
            when {
                not {
                    environment(name: "USER_UPDATED", value: "[]")
                }
            }
            steps {
                dir('user-service') {
                    script {
                        PROJECT_VERSION = sh(
                                script: './gradlew properties | grep -e ^version: | cut -d ":" -f 2 | sed -e "s/^ *//g" -e "s/ *$//g"',
                                returnStdout: true
                        ).trim()
                    }

                    build job: 'come-on-service-dev',
                            parameters: [
                                    string(name: 'project_name', value: 'user-service'),
                                    string(name: 'project_version', value: PROJECT_VERSION),
                                    booleanParam(name: 'test_flag', value: true)
                            ]
                }
            }
        }

        stage('api-gateway-service') {
            when {
                not {
                    environment(name: "API_GATEWAY_UPDATED", value: "[]")
                }
            }
            steps {
                dir('api-gateway-service') {
                    script {
                        PROJECT_VERSION = sh(
                                script: './gradlew properties | grep -e ^version: | cut -d ":" -f 2 | sed -e "s/^ *//g" -e "s/ *$//g"',
                                returnStdout: true
                        ).trim()
                    }

                    build job: 'come-on-service-dev',
                            parameters: [
                                    string(name: 'project_name', value: 'api-gateway-service'),
                                    string(name: 'project_version', value: PROJECT_VERSION),
                                    booleanParam(name: 'test_flag', value: true)
                            ]
                }
            }
        }

        stage('docker image pull & run') {
            parallel {
                stage('config instance') {
                    steps {
                        echo 'service-instance docker image pull & run'
                        sshagent(['comeon-ec2-config-instance']) {
                            withCredentials([string(credentialsId: 'comeon-ec2-config-instance-addr', variable: 'config_instance_addr')]) {
                                script {
                                    def flag = 0

                                    if (env.API_GATEWAY_UPDATED != '[]') {
                                        flag = 1
                                        sh "ssh -o StrictHostKeyChecking=no $config_instance_addr 'docker rm -f api-gateway-service'"
                                        sh "ssh -o StrictHostKeyChecking=no $config_instance_addr 'docker rmi -f come0on/api-gateway-service:latest'"
                                    }

                                    if (flag == 1) {
                                        sh "ssh -o StrictHostKeyChecking=no $config_instance_addr 'docker ps'"
                                        sh "ssh -o StrictHostKeyChecking=no $config_instance_addr 'docker images'"

                                        sh "ssh -o StrictHostKeyChecking=no $config_instance_addr 'docker compose -f /etc/config/docker_compose/docker-compose.yml up -d --no-recreate'"

                                        sh "ssh -o StrictHostKeyChecking=no $config_instance_addr 'docker ps'"
                                        sh "ssh -o StrictHostKeyChecking=no $config_instance_addr 'docker images'"
                                    }
                                }

                                echo "config-instance pull & run END"
                            }
                        }
                    }
                }

                stage('service instance-1') {
                    steps {
                        echo 'service-instance-1 docker image pull & run'
                        sshagent(['comeon-ec2-service-instance']) {
                            withCredentials([string(credentialsId: 'comeon-ec2-service-instance-addr', variable: 'service_instance_addr')]) {
                                script {
                                    def flag = 0

                                    if (env.MEETING_UPDATED != '[]') {
                                        flag = 1
                                        sh "ssh -o StrictHostKeyChecking=no $service_instance_addr 'docker rm -f meeting-service'"
                                        sh "ssh -o StrictHostKeyChecking=no $service_instance_addr 'docker rmi -f come0on/meeting-service:latest'"
                                    }

                                    if (env.AUTH_UPDATED != '[]') {
                                        flag = 1
                                        sh "ssh -o StrictHostKeyChecking=no $service_instance_addr 'docker rm -f auth-service'"
                                        sh "ssh -o StrictHostKeyChecking=no $service_instance_addr 'docker rmi -f come0on/auth-service:latest'"
                                    }

                                    if (flag == 1) {
                                        sh "ssh -o StrictHostKeyChecking=no $service_instance_addr 'docker ps'"
                                        sh "ssh -o StrictHostKeyChecking=no $service_instance_addr 'docker images'"

                                        sh "ssh -o StrictHostKeyChecking=no $service_instance_addr 'docker compose -f /etc/config/docker/docker-compose.yml up -d --no-recreate'"

                                        sh "ssh -o StrictHostKeyChecking=no $service_instance_addr 'docker ps'"
                                        sh "ssh -o StrictHostKeyChecking=no $service_instance_addr 'docker images'"
                                    }
                                }

                                echo "service-instance-1 pull & run END"
                            }
                        }
                    }
                }

                stage('service instance-2') {
                    steps {
                        echo 'service-instance-2 docker image pull & run'
                        sshagent(['comeon-ec2-service-instance-2']) {
                            withCredentials([string(credentialsId: 'comeon-ec2-service-instance2-addr', variable: 'service_instance2_addr')]) {
                                script {
                                    def flag = 0

                                    if (env.USER_UPDATED != '[]') {
                                        flag = 1
                                        sh "ssh -o StrictHostKeyChecking=no $service_instance2_addr 'docker rm -f user-service'"
                                        sh "ssh -o StrictHostKeyChecking=no $service_instance2_addr 'docker rmi -f come0on/user-service:latest'"
                                    }

                                    if (env.COURSE_UPDATED != '[]') {
                                        flag = 1
                                        sh "ssh -o StrictHostKeyChecking=no $service_instance2_addr 'docker rm -f course-service'"
                                        sh "ssh -o StrictHostKeyChecking=no $service_instance2_addr 'docker rmi -f come0on/course-service:latest'"
                                    }

                                    if (flag == 1) {
                                        sh "ssh -o StrictHostKeyChecking=no $service_instance2_addr 'docker ps'"
                                        sh "ssh -o StrictHostKeyChecking=no $service_instance2_addr 'docker images'"

                                        sh "ssh -o StrictHostKeyChecking=no $service_instance2_addr 'docker compose -f /etc/config/docker/docker-compose.yml up -d --no-recreate'"

                                        sh "ssh -o StrictHostKeyChecking=no $service_instance2_addr 'docker ps'"
                                        sh "ssh -o StrictHostKeyChecking=no $service_instance2_addr 'docker images'"
                                    }
                                }

                                echo "service-instance-2 pull & run END"
                            }
                        }
                    }
                }
            }
        }
    }
    post {
        always {
            sh 'echo $BUILDX_BUILDER'
            sh "docker buildx rm $BUILDX_BUILDER --keep-state"
        }
        success {
            slackSend(
                    channel: SLACK_CHANNEL,
                    color: '#00FF00',
                    message: "SUCCESS: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})",
                    teamDomain: TEAM_DOMAIN,
                    tokenCredentialId: SLACK_TOKEN_ID
            )
        }
        failure {
            slackSend(
                    channel: SLACK_CHANNEL,
                    color: '#F01717',
                    message: "FAIL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})",
                    teamDomain: TEAM_DOMAIN,
                    tokenCredentialId: SLACK_TOKEN_ID
            )
        }
    }
}
```


## Freestyle Job : come-on-service-dev
Pipeline Job인 "come-on-service"에 의해서 호출됩니다.
프로젝트 테스트, 빌드, 도커 이미지 빌드 & 업로드를 수행합니다.

```shell
#### task1 : test_flag 파라미터에 따라 테스트 수행
if [ "$test_flag" = "true" ]; then
	echo "$project_name test start" 
	cd ./$project_name
	# test
	./gradlew clean test -Pprofile=test
else
	echo "$project_name test skip"
fi

#### task2 : 프로젝트 빌드 수행
echo "$project_name gradle build start"
cd ./$project_name
# gradle build
./gradlew bootJar

#### task3 : 프로젝트 빌드 결과로 도커 이미지 빌드 & push
echo "$project_name docker image build & push start"
cd ./$project_name
docker login --username=$docker_id --password=$docker_pw
# version 태그와 latest 태그로 push
docker buildx build --platform linux/arm64/v8,linux/amd64 --tag $docker_id/$project_name:$project_version --tag $docker_id/$project_name:latest --push .
```