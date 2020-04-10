provider "aws" {
  profile    = "default"
  region     = "eu-west-2"
  access_key = var.aws_access_key_id
  secret_key = var.aws_secret_access_key
  version    = "2.56"
}

data "aws_caller_identity" "current" {}

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

  snapshot_options {
    automated_snapshot_start_hour = 23
  }

  node_to_node_encryption {
    enabled = true
  }
}
