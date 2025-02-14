
<h3 align="center">Demoapi</h3>

  <p align="center">
    Simple project for learning few new technologies

<!-- ABOUT PROJECT -->
## About Project
### Project prerequisites
* use [Flux](https://fluxcd.io/) for deployment and maintenance of simple application
* simple aplication - rest api (basic CRUD methods) using Java Spring
* use Mongodb/PostgreSQL or Kafka (you also can use both)
* deploy everything on Google Cloud Platform
* you have one month to complete the project

### Project final version
*  Above prerequisites has been fulfilled
*  Additionally I expanded functionallity above the prerequisites. Flux has been set up to automatically update changes in deployment (Kubernetes) but also version of the application. Basically when you are making a commit to the source code github action will build docker image of the application and store it in docker repository. Flux is listening to docker repository and when it will get a new application version it will be deployed automatically
*  I have not used GKE (Google Kubernetes Engine) intentionally. I wanted to set up Kubernetes on my own on GCE (Google Compute Engine - virtual machines). I think it was a fun challenge. Due to cost and simple installation I used k3s (light version of Kubernetes). Not using GKE has made it difficult to implement Ingres. Instead I implemented application as NodePort.
*  The project currently does not exist on GCP due to costs.

<!-- GETTING STARTED -->
## Cheat sheet for project owner

### Creating and pushing a new Docker image

1. Create docker image from **demoapi** directory
    ```sh
    docker build -t x1023/demoapi:latest .
    ```
2. Verify image
    ```sh
    docker images
   ``` 
3. Push docker image
    ```sh
    docker push x1023/demoapi:latest
   ```

### Kubernetes prerequisites

**For locally testing I used [docker desktop](https://www.docker.com/products/docker-desktop/) <br>
Install it first following [installation guide](https://docs.docker.com/desktop/install/windows-install/) - this is for windows but you can change system using left toolbar.** 

When mongodb is run for the first time, a root user is created with **MONGO_INITDB_ROOT_USERNAME** and **MONGO_INITDB_ROOT_PASSWORD** variables. <br>
They are stored in Kubernetes secrets. You need to create secret for root credentials.

1.  Create Kubernetes secret, replace **<root_password>** and **<root_username>** with proper values
    ```sh
    kubectl create secret generic mongo-root-credentials --namespace demo \
    --from-literal=MONGO_INITDB_ROOT_USERNAME=<root_username> \
    --from-literal=MONGO_INITDB_ROOT_PASSWORD=<root_password>
    ```
2. Run Kustomization from **demoapi\k8s\clusters\demo** directory
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

### GCP connection form Windows prerequisites
1. Create variable with path to credentials for terraform service
   ```sh
    $env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\terraform-sa-key.json"
    ```

### GCP set up the new VM (new Kubernetes cluster)
1. Install k3s
   ```sh
    curl -sfL https://get.k3s.io | sh -
    ```
2. After running the installation script, check if the k3s service is running:
   ```sh
    sudo systemctl status k3s
    ```
3. Ensure the kubeconfig file is in place and accessible. By default, K3s places the kubeconfig file at /etc/rancher/k3s/k3s.yaml.
You should copy this file to the default kubeconfig location or set the KUBECONFIG environment variable.
   ```sh
   mkdir -p $HOME/.kube
   sudo cp /etc/rancher/k3s/k3s.yaml $HOME/.kube/config
   sudo chown $(id -u):$(id -g) $HOME/.kube/config
   export KUBECONFIG=$HOME/.kube/config
   ```
4. Install Flux
   ```sh
    curl -s https://fluxcd.io/install.sh | sudo bash
    ```
5. Create repo directory for repository in home directory
   ```sh
    mkdir ~/repo
    ```
6. Pull repository (use GitHub PAT)
   ```sh
    cd ~/repo && git clone https://github.com/itmarcin/demoapi.git
    ```
7. Create demo namespace
   ```sh
    cd ~/repo/demoapi/k8s/clusters/demo && kubectl apply -f demo-namespace.yaml
    ```
8. Create demo namespace
   ```sh
   kubectl create namespace flux-system
    ```
9. Create Kubernetes secret for mongodb credentials, replace **<root_password>** and **<root_username>** with proper values.
    ```sh
    kubectl create secret generic mongo-root-credentials --namespace demo --from-literal=MONGO_INITDB_ROOT_USERNAME=<root_username> --from-literal=MONGO_INITDB_ROOT_PASSWORD=<root_password>
10. Create Kubernetes secret for GitHub PAT used by flux for automatic demoapi version commits.
    ```sh
    kubectl create secret generic flux-git-auth --namespace=flux-system --from-literal=username=itmarcin --from-literal=password=<gcp-demoapi-flux-admin>
     ``` 
11. Bootstrap repository - use PAT in gcp-demoapi-flux-admin
    ```sh
     echo "gcp-demoapi-flux-admin" | flux bootstrap github --token-auth --owner=itmarcin --repository=demoapi --branch=main --path=./k8s/clusters/demo --components=source-controller,kustomize-controller,helm-controller,image-reflector-controller,image-automation-controller --personal
     ```







