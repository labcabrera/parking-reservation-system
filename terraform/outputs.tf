output "keycloak_realm_id" {
  description = "Internal Keycloak ID of the created realm."
  value       = module.keycloak.realm_id
}

output "keycloak_realm_name" {
  description = "Name of the created Keycloak realm."
  value       = module.keycloak.realm_name
}

output "parking_client_id" {
  description = "Client ID of parking-client."
  value       = module.keycloak.parking_client_id
}

output "parking_client_secret" {
  description = "Client secret of parking-client."
  value       = module.keycloak.parking_client_secret
  sensitive   = true
}
