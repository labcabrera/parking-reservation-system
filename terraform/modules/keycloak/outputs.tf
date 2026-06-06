output "realm_id" {
  description = "Internal Keycloak ID of the created realm."
  value       = keycloak_realm.parking.id
}

output "realm_name" {
  description = "Name of the created realm."
  value       = keycloak_realm.parking.realm
}

output "group_parking_admin_id" {
  description = "ID of the parking-admin group."
  value       = keycloak_group.parking_admin.id
}

output "group_parking_pricing_admin_id" {
  description = "ID of the parking-pricing-admin group."
  value       = keycloak_group.parking_pricing_admin.id
}

output "parking_client_id" {
  description = "Client ID of parking-client."
  value       = keycloak_openid_client.parking_client.client_id
}

output "parking_client_secret" {
  description = "Client secret of parking-client."
  value       = keycloak_openid_client.parking_client.client_secret
  sensitive   = true
}
