data "aws_iam_policy_document" "trust_cognito_unauth" {
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
      values   = ["unauthenticated"]
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

resource "aws_iam_role" "cognito_kibana_unauth" {
  assume_role_policy = data.aws_iam_policy_document.trust_cognito_unauth.json
  name               = "Cognito_kibanaUnauth_Role"
}

data "aws_iam_policy_document" "cognito_unauth" {
  statement {
    effect    = "Allow"
    resources = ["*"]
    actions = [
      "mobileanalytics:PutEvents",
      "cognito-sync:*"
    ]
  }
}

resource "aws_iam_role_policy" "cognito_unauth" {
  role   = aws_iam_role.cognito_kibana_unauth.id
  policy = data.aws_iam_policy_document.cognito_unauth.json
}