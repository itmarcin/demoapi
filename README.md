
<h3 align="center">Demoapi</h3>

  <p align="center">
    Simple project for learning few new technologies


<!-- GETTING STARTED -->
## Getting Started



### Prerequisites

When mongodb is run for the first time, a root user is created with **MONGO_INITDB_ROOT_USERNAME** and **MONGO_INITDB_ROOT_PASSWORD** variables. <br>
They are stored in Kubernetes secrets. You need to create secret for root credentials.

1.  Create Kubernetes secret, replace **<root_password>** and **<root_username>** with proper values
    ```sh
    kubectl create secret generic mongo-root-credentials --namespace demo \
    --from-literal=MONGO_INITDB_ROOT_USERNAME=<root_username> \
    --from-literal=MONGO_INITDB_ROOT_PASSWORD=<root_password>
    ```
2. Run Kustomization from **demoapi\k8s\clusters** directory
    ```sh
    kubectl kustomize . | kubectl apply -f -
   ```    
3. Check if pods has been created
   ```sh
    kubectl get pods -n demo
    ```
4. Output should be similar to (you should wait about 30 seconds for demoapi to resolve all connections)
   ```sh
    NAME                                  READY   STATUS    RESTARTS   AGE
    demoapi-deployment-756684f445-g2mxn   1/1     Running   0          49m
    kafka-deployment-5887487b99-5fk6k     1/1     Running   0          51m
    mongo-deployment-7dfb47ddb5-zlmb5     1/1     Running   0          51m
    ```
5. You can check logs. Replace **demoapi-deployment-756684f445-g2mxn** with your demoapi pod.
   ```sh
    kubectl logs demoapi-deployment-756684f445-g2mxn -n demo
    ```   

<p align="right">(<a href="#readme-top">back to top</a>)</p>





