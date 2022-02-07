# Microservices Homework 2
This repo contains a simple CRUD Rest service written in Scala 2.13. The forder UserService contains the source code of the service and docker files. You can use docker-compose.yml file for debug the service with Postgresql database.
The application uses Cats Effects 3, Log4Cats, Cats.Core, Doobie, Quill.

## Prerequisites
To run this example you need following software installed:
1. Docker 
2. Minikube (following addons needs to be enabled:  default-storageclass,  ingress,  storage-provisioner)
3. Helm
4. SBT (Optionally if you aim to build scala application manually)
5. Newman or Postman (Optionnally, if you want to launch postman collection)

## Building the docker image
_Note: This step can be skipped as an image is already published to dockerhub._<br>

There are two ways of building the image
1. If you have SBT installed - build the native image with commands:<br>
`$ sbt assembly`<br>
`$ docker build --build-arg port=8000 -t users:0.1.0 -f Dockerfile.javabuild .`<br>
2. Using docker build command (staged build):
`$ docker buildx build --build-arg port=8000 -t users:0.1.1 -f Dockerfile.staged .`

## Install the application
1. Download Postgresql dependency by runnnig command <br> `helm dependency update userservice-chart`
2. Run command <br> `helm install first-release userservice-chart`
The database will be installed as a dependency of userservice-chart.
3. Esposing a service to localhost (Windows): `minikube tunnel`
4. Setting up an arch.homework host - please add `127.0.0.1   arch.homework` to your hosts file (C:\windows\system32\drivers\etc\hosts on Windows)

## Running test
_Note: Before running tests please wait for 20-30 seconds to allow application to start
1. Ensure that minikube tunnel is estabilihed ( command `minikube tunnel`)
2. Change directory to manifest
3. Launch newman tests: `newman run Homework-Users.postman_collection.json`
