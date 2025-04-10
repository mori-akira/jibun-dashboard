terraform {
  backend "s3" {
    bucket  = "jibun-dashboard-tf-state"
    key     = "jibun-dashboard/terraform.tfstate"
    region  = "ap-northeast-1"
    encrypt = true
  }
}
