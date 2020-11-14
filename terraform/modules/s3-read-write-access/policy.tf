data "aws_iam_policy_document" "read_write_access" {
  statement {
    actions = [
      "s3:DeleteObject",
      "s3:GetObject",
      "s3:PutObject",
    ]

    effect    = "Allow"
    resources = ["${var.bucket_arn}/*"]

    principals {
      type        = "AWS"
      identifiers = [var.user_arn]
    }
  }

  statement {
    actions = [
      "s3:ListBucket",
    ]

    effect    = "Allow"
    resources = [var.bucket_arn]

    principals {
      type        = "AWS"
      identifiers = [var.user_arn]
    }
  }
}

resource "aws_s3_bucket_policy" "read_write_access" {
  bucket = var.bucket_id
  policy = data.aws_iam_policy_document.read_write_access.json
}