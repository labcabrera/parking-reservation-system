resource "postgresql_database" "databases" {
  for_each = toset(var.databases)

  name              = each.key
  owner             = var.owner
  connection_limit  = -1
  allow_connections = true
}
