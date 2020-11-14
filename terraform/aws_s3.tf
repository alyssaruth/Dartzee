resource "aws_iam_user" "dartzee_sync_user" {
  name = "dartzee-sync"
}

resource "aws_iam_access_key" "dartzee_sync_user" {
  user = aws_iam_user.dartzee_sync_user.name
}

resource "aws_s3_bucket" "unit_test" {
  bucket = "dartzee-unit-test"
}

resource "aws_s3_bucket" "dartzee_databases" {
  bucket = "dartzee-databases"
}

module "unit_test_access" {
  source = "./modules/s3-read-write-access"

  bucket_id  = aws_s3_bucket.unit_test.id
  bucket_arn = aws_s3_bucket.unit_test.arn
  user_arn   = aws_iam_user.dartzee_sync_user.arn
}

module "dartzee_databases_access" {
  source = "./modules/s3-read-write-access"

  bucket_id  = aws_s3_bucket.dartzee_databases.id
  bucket_arn = aws_s3_bucket.dartzee_databases.arn
  user_arn   = aws_iam_user.dartzee_sync_user.arn
}