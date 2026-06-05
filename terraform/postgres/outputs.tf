output "database_names" {
  description = "Names of the created databases."
  value       = [for db in postgresql_database.databases : db.name]
}
