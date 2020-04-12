data aws_iam_policy_document "trust_amazon_es" {
  statement {
    actions = [
      "sts:AssumeRole",
    ]

    effect = "Allow"

    principals {
      type = "Service"

      identifiers = [
        "es.amazonaws.com",
      ]
    }
  }
}

resource "aws_iam_role" "cognito_access_for_amazon_es" {
  assume_role_policy = data.aws_iam_policy_document.trust_amazon_es.json
  name               = "CognitoAccessForAmazonES"
  path               = "/service-role/"
  description        = "Amazon Elasticsearch role for Kibana authentication."
}

resource "aws_iam_role_policy_attachment" "aws_es_cognito_access" {
  role       = aws_iam_role.cognito_access_for_amazon_es.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonESCognitoAccess"
}