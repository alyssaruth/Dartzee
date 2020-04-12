resource "aws_cognito_user_pool" "kibana" {
  name = "kibana"

  admin_create_user_config {
    allow_admin_create_user_only = true
  }

  password_policy {
    minimum_length                   = 8
    require_lowercase                = true
    require_numbers                  = true
    require_symbols                  = true
    require_uppercase                = true
    temporary_password_validity_days = 7
  }

  username_configuration {
    case_sensitive = false
  }
}

resource "aws_cognito_user_pool_domain" "kibana" {
  user_pool_id = aws_cognito_user_pool.kibana.id
  domain       = "dartzee"
}

resource "aws_cognito_user_pool_client" "dartzee" {
  name         = "AWSElasticsearch-dartzee-${var.aws_region}-${var.aws_elasticsearch_uuid}"
  user_pool_id = aws_cognito_user_pool.kibana.id

  allowed_oauth_flows                  = ["code"]
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_scopes                 = ["email", "openid", "phone", "profile"]
  explicit_auth_flows                  = []

  supported_identity_providers = ["COGNITO"]

  callback_urls = ["https://search-dartzee-${var.aws_elasticsearch_uuid}.${var.aws_region}.es.amazonaws.com/_plugin/kibana/app/kibana"]
  logout_urls   = ["https://search-dartzee-${var.aws_elasticsearch_uuid}.${var.aws_region}.es.amazonaws.com/_plugin/kibana/app/kibana"]
}

resource "aws_cognito_identity_pool" "kibana" {
  identity_pool_name               = "kibana"
  allow_unauthenticated_identities = false

  cognito_identity_providers {
    client_id               = aws_cognito_user_pool_client.dartzee.id
    provider_name           = aws_cognito_user_pool.kibana.endpoint
    server_side_token_check = false
  }
}

resource "aws_cognito_identity_pool_roles_attachment" "unauth" {
  identity_pool_id = aws_cognito_identity_pool.kibana.id

  roles = {
    authenticated   = aws_iam_role.cognito_kibana_auth.arn
    unauthenticated = aws_iam_role.cognito_kibana_unauth.arn
  }
}