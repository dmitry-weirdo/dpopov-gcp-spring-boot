# Basic GCP technologies stack

## To start
* Clone the repository.
* Add json file with your GCP-project credentials to ``/src/main/resources``.
* Set the name of this json file to maven ``gcp.credentials.file`` property.

## Typical build process
* Build maven artifact - run ``mvn clean install``.
* Build docker image - run ``mvn dockerfile:build``.
* You can check that image is created in ``docker images``.
* Run docker container with open debug port locally (replace version tag with your version accordingly to ``<tag>`` in maven):  
    ``docker run -p 8080:8080 -p 8000:8000 -t gcr.io/dpopov-gcp-spring-boot/gs-spring-boot-docker:<VERSION>``
* Application will be accessible on <http://localhost:8080/>
* You can connect to your running docker container with bash: 
  ``docker exec -it <CONTAINER_ID> /bin/bash``   
* Push docker image to Kubernetes Engine: 
  ``docker push gcr.io/dpopov-gcp-spring-boot/gs-spring-boot-docker:<VERSION>``
* Update pods in the Kubernetes service: 
  ``kubectl set image deployment/spring-boot-service spring-boot-service=gcr.io/dpopov-gcp-spring-boot/gs-spring-boot-docker:<VERSION>``
* Run ``kubectl get services`` to get the external ip of service ``spring-boot-service``.   
* App will be accessible on external ip of the service, port 80.