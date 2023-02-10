provider "aws" {
  profile    = "default"
  region     = var.aws_region
  access_key = var.aws_access_key_id
  secret_key = var.aws_secret_access_key
}

data aws_caller_identity "current" {}

resource "aws_elasticsearch_domain" "dartzee" {
  domain_name           = "dartzee"
  elasticsearch_version = "7.4"

  cluster_config {
    instance_type = "t2.small.elasticsearch"
  }

  ebs_options {
    ebs_enabled = true
    volume_size = 10
    volume_type = "standard"
  }

  cognito_options {
    enabled          = true
    identity_pool_id = aws_cognito_identity_pool.kibana.id
    role_arn         = aws_iam_role.cognito_access_for_amazon_es.arn
    user_pool_id     = aws_cognito_user_pool.kibana.id
  }

  snapshot_options {
    automated_snapshot_start_hour = 23
  }

  node_to_node_encryption {
    enabled = true
  }
}

resource "aws_iam_user" "elasticsearch" {
  name = "elasticsearch"
}

resource "aws_iam_access_key" "elasticsearch" {
  user = aws_iam_user.elasticsearch.name
}

data "aws_iam_policy_document" "elasticsearch_put" {
  statement {
    effect = "Allow"

    actions = ["es:ESHttpPut"]

    resources = [
      "${aws_elasticsearch_domain.dartzee.arn}/dartzee/*",
      "${aws_elasticsearch_domain.dartzee.arn}/unittest/*"
    ]
  }

  statement {
    effect = "Allow"

    actions = ["es:ESHttpGet"]

    resources = [
      "${aws_elasticsearch_domain.dartzee.arn}/_cluster/health"
    ]
  }
}

resource "aws_iam_user_policy" "elasticsearch" {
  user   = aws_iam_user.elasticsearch.name
  policy = data.aws_iam_policy_document.elasticsearch_put.json
}