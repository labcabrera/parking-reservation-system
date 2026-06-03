variable "keycloak_url" {
  description = "Base URL of the Keycloak instance (e.g. http://localhost:8080)."
  type        = string
}

variable "keycloak_admin_username" {
  description = "Keycloak admin username."
  type        = string
  default     = "admin"
}

variable "keycloak_admin_password" {
  description = "Keycloak admin password."
  type        = string
  sensitive   = true
}

variable "realm_name" {
  description = "Name of the Keycloak realm to create."
  type        = string
  default     = "parking"
}

variable "realm_display_name" {
  description = "Display name of the realm shown in the Keycloak UI."
  type        = string
  default     = "Parking Platform"
}

variable "parking_client_secret" {
  description = "Client secret for parking-client."
  type        = string
  sensitive   = true
}

variable "parking_client_valid_redirect_uris" {
  description = "List of valid redirect URIs for parking-client."
  type        = list(string)
  default     = ["http://localhost:*/*"]
}

variable "parking_client_web_origins" {
  description = "List of allowed web origins for parking-client (CORS)."
  type        = list(string)
  default     = ["http://localhost:*"]
}

variable "parking_client_front_valid_redirect_uris" {
  description = "List of valid redirect URIs for parking-client-front."
  type        = list(string)
  default     = ["http://localhost:8080/*"]
}

variable "parking_client_front_web_origins" {
  description = "List of allowed web origins for parking-client-front (CORS)."
  type        = list(string)
  default     = ["http://localhost:*"]
}

variable "primary_user_username" {
  description = "Username for the primary user."
  type        = string
}

variable "primary_user_email" {
  description = "Email for the primary user."
  type        = string
}

variable "primary_user_first_name" {
  description = "First name of the primary user."
  type        = string
}

variable "primary_user_last_name" {
  description = "Last name of the primary user."
  type        = string
}

variable "primary_user_password" {
  description = "Password for the primary user."
  type        = string
  sensitive   = true
}

variable "guest_user_username" {
  description = "Username for the guest user."
  type        = string
  default     = "guest"
}

variable "guest_user_email" {
  description = "Email for the guest user."
  type        = string
}

variable "guest_user_first_name" {
  description = "First name of the guest user."
  type        = string
}

variable "guest_user_last_name" {
  description = "Last name of the guest user."
  type        = string
}

variable "guest_user_password" {
  description = "Password for the guest user."
  type        = string
  sensitive   = true
}
