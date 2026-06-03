# -----------------------------------------------------------------------------
# Realm
# -----------------------------------------------------------------------------

resource "keycloak_realm" "parking" {
  realm        = var.realm_name
  enabled      = true
  display_name = var.realm_display_name

  registration_allowed           = true
  registration_email_as_username = false
  reset_password_allowed         = true
  remember_me                    = false
  verify_email                   = false
  login_with_email_allowed       = true
  duplicate_emails_allowed       = false

  ssl_required = "external"

  access_token_lifespan                   = "24h"
  access_token_lifespan_for_implicit_flow = "24h"
  sso_session_idle_timeout                = "30m"
  sso_session_max_lifespan                = "10h"
  offline_session_idle_timeout            = "720h"
  offline_session_max_lifespan_enabled    = false
}

# -----------------------------------------------------------------------------
# Groups
# -----------------------------------------------------------------------------

resource "keycloak_group" "parking_admin" {
  realm_id = keycloak_realm.parking.id
  name     = "parking-admin"
}

resource "keycloak_group" "parking_pricing_admin" {
  realm_id = keycloak_realm.parking.id
  name     = "parking-pricing-admin"
}

# -----------------------------------------------------------------------------
# Client: parking-client (confidential / authentication enabled)
# -----------------------------------------------------------------------------

resource "keycloak_openid_client" "parking_client" {
  realm_id  = keycloak_realm.parking.id
  client_id = "parking-client"
  name      = "Parking Client"
  enabled   = true

  access_type                  = "CONFIDENTIAL"
  standard_flow_enabled        = true
  implicit_flow_enabled        = false
  direct_access_grants_enabled = true
  service_accounts_enabled     = true

  client_secret = var.parking_client_secret

  valid_redirect_uris = var.parking_client_valid_redirect_uris
  web_origins         = var.parking_client_web_origins
}

# -----------------------------------------------------------------------------
# Mappers: parking-client dedicated scope
# -----------------------------------------------------------------------------

resource "keycloak_openid_hardcoded_claim_protocol_mapper" "parking_client_groups" {
  realm_id  = keycloak_realm.parking.id
  client_id = keycloak_openid_client.parking_client.id
  name      = "hardcoded-groups"

  claim_name       = "groups"
  claim_value      = jsonencode(["parking-admin", "parking-pricing-admin"])
  claim_value_type = "JSON"

  add_to_id_token     = false
  add_to_access_token = true
  add_to_userinfo     = false
}

# -----------------------------------------------------------------------------
# Client scope: groups
# -----------------------------------------------------------------------------

resource "keycloak_openid_client_scope" "groups" {
  realm_id    = keycloak_realm.parking.id
  name        = "groups"
  description = "Includes the groups the user belongs to as a claim in the token."
}

resource "keycloak_openid_group_membership_protocol_mapper" "groups_mapper" {
  realm_id        = keycloak_realm.parking.id
  client_scope_id = keycloak_openid_client_scope.groups.id
  name            = "groups"

  claim_name          = "groups"
  full_path           = false
  add_to_id_token     = true
  add_to_access_token = true
  add_to_userinfo     = true
}

# -----------------------------------------------------------------------------
# Service account role: assign realm-admin to parking-client
# -----------------------------------------------------------------------------

data "keycloak_openid_client" "realm_management" {
  realm_id  = keycloak_realm.parking.id
  client_id = "realm-management"
}

data "keycloak_role" "realm_admin" {
  realm_id  = keycloak_realm.parking.id
  client_id = data.keycloak_openid_client.realm_management.id
  name      = "realm-admin"
}

resource "keycloak_openid_client_service_account_role" "parking_client_realm_admin" {
  realm_id                = keycloak_realm.parking.id
  service_account_user_id = keycloak_openid_client.parking_client.service_account_user_id
  client_id               = data.keycloak_openid_client.realm_management.id
  role                    = data.keycloak_role.realm_admin.name
}

# -----------------------------------------------------------------------------
# Assign client scope to parking-client (default)
# -----------------------------------------------------------------------------

resource "keycloak_openid_client_default_scopes" "parking_client_default_scopes" {
  realm_id  = keycloak_realm.parking.id
  client_id = keycloak_openid_client.parking_client.id

  default_scopes = [
    "basic",
    "profile",
    "email",
    "roles",
    "web-origins",
    keycloak_openid_client_scope.groups.name,
  ]
}

# -----------------------------------------------------------------------------
# Client: parking-client-front (public / PKCE S256 / SPA)
# -----------------------------------------------------------------------------

resource "keycloak_openid_client" "parking_client_front" {
  realm_id  = keycloak_realm.parking.id
  client_id = "parking-client-front"
  name      = "Parking Client Front"
  enabled   = true

  access_type                  = "PUBLIC"
  standard_flow_enabled        = true
  implicit_flow_enabled        = false
  direct_access_grants_enabled = false
  service_accounts_enabled     = false

  pkce_code_challenge_method = "S256"

  valid_redirect_uris = var.parking_client_front_valid_redirect_uris
  web_origins         = var.parking_client_front_web_origins
}

resource "keycloak_openid_client_default_scopes" "parking_client_front_default_scopes" {
  realm_id  = keycloak_realm.parking.id
  client_id = keycloak_openid_client.parking_client_front.id

  default_scopes = [
    "basic",
    "profile",
    "email",
    "roles",
    "web-origins",
    keycloak_openid_client_scope.groups.name,
  ]
}

# -----------------------------------------------------------------------------
# User: primary
# -----------------------------------------------------------------------------

resource "keycloak_user" "primary_user" {
  realm_id = keycloak_realm.parking.id
  username = var.primary_user_username
  enabled  = true

  email          = var.primary_user_email
  email_verified = true
  first_name     = var.primary_user_first_name
  last_name      = var.primary_user_last_name

  initial_password {
    value     = var.primary_user_password
    temporary = false
  }
}

resource "keycloak_user_groups" "primary_user_groups" {
  realm_id = keycloak_realm.parking.id
  user_id  = keycloak_user.primary_user.id

  group_ids = [
    keycloak_group.parking_admin.id,
    keycloak_group.parking_pricing_admin.id,
  ]
}

# -----------------------------------------------------------------------------
# User: guest
# -----------------------------------------------------------------------------

resource "keycloak_user" "guest_user" {
  realm_id = keycloak_realm.parking.id
  username = var.guest_user_username
  enabled  = true

  email          = var.guest_user_email
  email_verified = true
  first_name     = var.guest_user_first_name
  last_name      = var.guest_user_last_name

  initial_password {
    value     = var.guest_user_password
    temporary = false
  }
}

# -----------------------------------------------------------------------------
# User profile: make firstName and lastName optional
# -----------------------------------------------------------------------------

resource "keycloak_realm_user_profile" "parking" {
  realm_id = keycloak_realm.parking.id

  attribute {
    name         = "username"
    display_name = "$${username}"

    validator {
      name = "length"
      config = {
        min = "3"
        max = "255"
      }
    }
    validator {
      name = "username-prohibited-characters"
    }
    validator {
      name = "up-username-not-idn-homograph"
    }

    permissions {
      view = ["admin", "user"]
      edit = ["admin", "user"]
    }

    required_for_roles = ["user"]
  }

  attribute {
    name         = "email"
    display_name = "$${email}"

    validator {
      name = "email"
    }
    validator {
      name = "length"
      config = {
        max = "255"
      }
    }

    permissions {
      view = ["admin", "user"]
      edit = ["admin", "user"]
    }

    required_for_roles = ["user"]
  }

  attribute {
    name         = "firstName"
    display_name = "$${firstName}"

    permissions {
      view = ["admin", "user"]
      edit = ["admin", "user"]
    }
  }

  attribute {
    name         = "lastName"
    display_name = "$${lastName}"

    permissions {
      view = ["admin", "user"]
      edit = ["admin", "user"]
    }
  }
}
