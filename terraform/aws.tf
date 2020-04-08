provider "aws" {
  profile    = "default"
  region     = "eu-west-2"
  access_key = var.aws_access_key_id
  secret_key = var.aws_secret_access_key
  version    = "2.56"
}

data "aws_caller_identity" "current" {}

resource "aws_iam_user" "elasticsearch" {
  name = "elasticsearch"
}

resource "aws_iam_access_key" "elasticsearch" {
  user = aws_iam_user.elasticsearch.name
}

output "access_key" {
  value = aws_iam_access_key.elasticsearch.id
}

data "aws_iam_policy_document" "elasticsearch" {
  statement {
    actions = [
      "es:*",
    ]

    effect = "Allow"

    resources = [
      "arn:aws:es:eu-west-2:${data.aws_caller_identity.current.account_id}:domain/dartzee/*",
    ]

    principals {
      type = "AWS"

      identifiers = [
        aws_iam_user.elasticsearch.arn,
      ]
    }
  }
}

resource "aws_elasticsearch_domain" "dartzee" {
  domain_name           = "dartzee"
  elasticsearch_version = "7.4"

  cluster_config {
    instance_type = "r5.large.elasticsearch"
  }

  ebs_options {
    ebs_enabled = true
    volume_size = 10
    volume_type = "standard"
  }

  snapshot_options {
    automated_snapshot_start_hour = 23
  }

  node_to_node_encryption {
    enabled = true
  }

  encrypt_at_rest {
    enabled = true
  }

  access_policies = data.aws_iam_policy_document.elasticsearch.json
}
