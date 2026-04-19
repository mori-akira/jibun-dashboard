variable "application_tag" {
  type = map(string)
}

variable "app_name" {
  type = string
}

variable "env_name" {
  type = string
}

variable "qualifications_table_name" {
  type = string
}

variable "qualifications_table_arn" {
  type = string
}

variable "lambda_timeout_seconds" {
  type    = number
  default = 300
}

variable "lambda_memory_size" {
  type    = number
  default = 256
}

variable "timezone" {
  type    = string
  default = "Asia/Tokyo"
}
