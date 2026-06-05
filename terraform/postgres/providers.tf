provider "postgresql" {
  host      = var.pg_host
  port      = var.pg_port
  username  = var.pg_superuser
  password  = var.pg_superuser_password
  sslmode   = "disable"
  superuser = false
}
