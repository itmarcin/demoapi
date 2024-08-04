# Define a custom VPC network
resource "google_compute_network" "custom_network" {
  name = "custom-network"
}

# Define a firewall rule to allow HTTP (port 80) and HTTPS (port 443) traffic.
# K3s is using 6443. NodePort of demoapi is using 30007.
resource "google_compute_firewall" "allow_http_k3s" {
  name    = "allow-http-k3s"
  network = google_compute_network.custom_network.name

  allow {
    protocol = "tcp"
    ports    = ["80", "443", "6443", "30007"]
  }

  source_ranges = ["0.0.0.0/0"]
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
