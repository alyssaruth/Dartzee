data aws_iam_policy_document "trust_cognito_auth" {
  statement {
    actions = [
      "sts:AssumeRoleWithWebIdentity",
    ]

    effect = "Allow"

    condition {
      test     = "StringEquals"
      values   = [aws_cognito_identity_pool.kibana.id]
      variable = "cognito-identity.amazonaws.com:aud"
    }

    condition {
      test     = "ForAnyValue:StringLike"
      values   = ["authenticated"]
      variable = "cognito-identity.amazonaws.com:amr"
    }

    principals {
      type = "Federated"

      identifiers = [
        "cognito-identity.amazonaws.com",
      ]
    }
  }
}

resource "aws_iam_role" "cognito_kibana_auth" {
  assume_role_policy = data.aws_iam_policy_document.trust_cognito_auth.json
  name               = "Cognito_kibanaAuth_Role"
}

data "aws_iam_policy_document" "kibana_access" {
  statement {
    actions = [
      "es:ESHttp*",
    ]

    effect = "Allow"

    resources = ["${aws_elasticsearch_domain.dartzee.arn}/*"]
  }
}

resource "aws_iam_policy" "kibana_access" {
  name        = "kibana_access"
  description = "Read and write access to kibana instance"
  policy      = data.aws_iam_policy_document.kibana_access.json
}

resource "aws_iam_role_policy_attachment" "cognito_kibana_access" {
  policy_arn = aws_iam_policy.kibana_access.arn
  role       = aws_iam_role.cognito_kibana_auth.name
}

data "aws_iam_policy_document" "cognito_auth" {
  statement {
    effect    = "Allow"
    resources = ["*"]
    actions = [
      "mobileanalytics:PutEvents",
      "cognito-sync:*",
      "cognito-identity:*"
    ]
  }
}

resource "aws_iam_role_policy" "cognito_auth" {
  role   = aws_iam_role.cognito_kibana_auth.id
  policy = data.aws_iam_policy_document.cognito_auth.json
}