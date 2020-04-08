provider "aws" {
  profile    = "default"
  region     = "eu-west-2"
  access_key = var.aws_access_key_id
  secret_key = var.aws_secret_access_key
  version    = "2.56"
}