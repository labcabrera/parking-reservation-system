provider "postgresql" {
  host      = var.pg_host
  port      = var.pg_port
  username  = var.pg_superuser
  password  = var.pg_superuser_password
  sslmode   = "disable"
  superuser = false
}

provider "keycloak" {
  client_id = "admin-cli"
  username  = var.keycloak_admin_username
  password  = var.keycloak_admin_password
  url       = var.keycloak_url
}
