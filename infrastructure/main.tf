
provider "google" {
  project = "hopeful-vine-431413-a1"
  region  = "europe-central2"
}

resource "google_compute_instance" "k8s_main" {
  name         = "k8s-main"
  machine_type = "e2-medium"
  zone         = "europe-central2-c"

  boot_disk {
    initialize_params {
      image = "projects/debian-cloud/global/images/family/debian-11"
    }
  }

  network_interface {
    network = google_compute_network.custom_network.name
    // It will generate external IP.
    access_config {}
  }

  metadata_startup_script = <<-EOF
  #! /bin/bash
    apt-get update
    apt-get install -y docker.io
    systemctl start docker
  EOF
}

# resource "google_compute_instance" "k8s_main_test" {
#   name         = "k8s-main-test"
#   machine_type = "e2-medium"
#   zone         = "europe-central2-c"
#
#   boot_disk {
#     initialize_params {
#       image = "projects/debian-cloud/global/images/family/debian-11"
#     }
#   }
#
#   network_interface {
#     network = google_compute_network.custom_network.name
#     access_config {}
#   }
#
#   metadata_startup_script = <<-EOF
#   #! /bin/bash
#     apt-get update
#     apt-get install -y docker.io
#     systemctl start docker
#   EOF
# }
