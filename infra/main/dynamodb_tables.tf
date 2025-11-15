locals {
  dynamodb_specs = {
    users = {
      table_name = "${var.app_name}-${var.env_name}-users"
      hash_key   = { name = "userId", type = "S" }
    }
    resources_i18n = {
      table_name = "${var.app_name}-${var.env_name}-resources-i18n"
      hash_key   = { name = "localeCode", type = "S" }
      sort_key   = { name = "messageKey", type = "S" }
    }
    settings = {
      table_name = "${var.app_name}-${var.env_name}-settings"
      hash_key   = { name = "userId", type = "S" }
    }
    salaries = {
      table_name = "${var.app_name}-${var.env_name}-salaries"
      hash_key   = { name = "userId", type = "S" }
      sort_key   = { name = "targetDate", type = "S" }
      gsi = [
        {
          name            = "gsi_salary_id",
          hash_key_name   = "salaryId",
          hash_key_type   = "S",
          projection_type = "ALL"
        }
      ]
    }
    qualifications = {
      table_name = "${var.app_name}-${var.env_name}-qualifications"
      hash_key   = { name = "userId", type = "S" }
      sort_key   = { name = "qualificationId", type = "S" }
      gsi = [
        {
          name            = "gsi_qualification_id",
          hash_key_name   = "qualificationId",
          hash_key_type   = "S",
          projection_type = "ALL"
        }
      ]
      lsi = [
        {
          name            = "lsi_order",
          range_key_name  = "order",
          range_key_type  = "N",
          projection_type = "ALL"
        }
      ]
    }
  }
}
