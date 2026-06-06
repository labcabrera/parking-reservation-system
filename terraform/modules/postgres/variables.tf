terraform {
  required_providers {
    postgresql = {
      source  = "cyrilgdn/postgresql"
      version = "~> 1.25"
    }
  }
}

variable "databases" {
  description = "List of databases to create."
  type        = list(string)
  default     = ["facilities_db", "ecommerce_db", "pricing_db"]
}

variable "owner" {
  description = "Owner (role) assigned to every created database."
  type        = string
}
