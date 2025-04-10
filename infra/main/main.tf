provider "aws" {
  region = var.region
}

module "application" {
  source = "./modules/application"
}

module "dynamodb" {
  source          = "./modules/dynamodb"
  application_tag = module.application.application_tag
}
