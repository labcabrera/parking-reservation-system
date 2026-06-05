variable "pg_host" {
  description = "PostgreSQL host."
  type        = string
  default     = "localhost"
}

variable "pg_port" {
  description = "PostgreSQL port."
  type        = number
  default     = 5432
}

variable "pg_superuser" {
  description = "PostgreSQL superuser username (matches POSTGRES_USER in docker-compose)."
  type        = string
  default     = "parking"
}

variable "pg_superuser_password" {
  description = "PostgreSQL superuser password."
  type        = string
  sensitive   = true
  default     = "parking"
}

variable "databases" {
  description = "List of databases to create."
  type        = list(string)
  default     = ["facilities", "ecommerce", "reservation_db", "payment_db"]
}
