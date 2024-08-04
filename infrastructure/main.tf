# Reference to the network and subnetwork resources defined in network.tf

provider "google" {
  project      = "hopeful-vine-431413-a1"
  region       = "europe-central2"
}

resource "google_compute_instance" "k8s_main" {
  name         = "k8s-main"
  machine_type = "e2-medium"
  zone         = "europe-central2-c"

  boot_disk {
    initialize_params {
      image = "projects/ml-images/global/images/c0-deeplearning-common-cpu-v20230925-debian-10"
    }
  }

  network_interface {
    network    = google_compute_network.custom_network.name
    access_config {}
  }

  metadata_startup_script = <<-EOF
    #! /bin/bash
    apt-get update
    apt-get install -y docker.io
    ufw disable
    systemctl start docker
    curl -sfL https://get.k3s.io | sh -
    kubectl taint nodes --all node-role.kubernetes.io/master-
  EOF
}
