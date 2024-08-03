# Define a custom VPC network
resource "google_compute_network" "custom_network" {
  name = "custom-network"
}

# Define a subnetwork within the custom VPC
# resource "google_compute_subnetwork" "internal_network" {
#   name          = "custom-subnetwork"
#   network       = google_compute_network.default.name
#   ip_cidr_range  = "10.0.0.0/24"
#   region        = "europe-central2"
# }

# Define a firewall rule to allow HTTP (port 80) and HTTPS (port 443) traffic. K3s is using 6443.
resource "google_compute_firewall" "allow_http_k3s" {
  name    = "allow-http-k3s"
  network = google_compute_network.custom_network.name

  allow {
    protocol = "tcp"
    ports    = ["80", "443", "6443"]
  }

  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["http-server"]
}

# Firewall rule to allow SSH from IAP
resource "google_compute_firewall" "allow_iap_ssh" {
  name    = "allow-iap-ssh"
  network = google_compute_network.custom_network.name

  allow {
    protocol = "tcp"
    ports = ["22"]
  }

  source_ranges = ["35.235.240.0/20"]
}

# Define a firewall rule to allow internal traffic within the network
# resource "google_compute_firewall" "allow_internal" {
#   name    = "allow-internal"
#   network = google_compute_network.internal_network.name
#
#   allow {
#     protocol = "tcp"
#     ports    = ["0-65535"] # Allow all ports
#   }
#
#   # Allow traffic from within the same network
#   source_ranges = ["10.0.0.0/24"]
# }
