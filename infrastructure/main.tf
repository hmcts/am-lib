provider "azurerm" {}

locals {
  ase_name               = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
  shortEnv = "${(var.env == "preview" || var.env == "spreview") ? var.deployment_namespace : var.env}"

}

module "am-api" {
  source              = "git@github.com:hmcts/moj-module-webapp?ref=master"
  product             = "${var.product}-${var.component}"
  location            = "${var.location_app}"
  env                 = "${var.env}"
  ilbIp               = "${var.ilbIp}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  subscription        = "${var.subscription}"
  is_frontend         = "${var.external_host_name != "" ? "1" : "0"}"
  additional_host_name = "${var.external_host_name != "" ? var.external_host_name : "null"}"
  capacity            = "${var.capacity}"
  instance_size       = "${var.instance_size}"
  common_tags         = "${var.common_tags}"

  app_settings = {
    LOGBACK_REQUIRE_ALERT_LEVEL = "false"
    LOGBACK_REQUIRE_ERROR_CODE  = "false"
  }
}

module "postgres-am-api" {
  source              = "git@github.com:hmcts/moj-module-postgres?ref=master"
  product             = "${var.product}-${var.component}"
  env                 = "${var.env}"
  location            = "${var.location_app}"
  postgresql_user     = "${var.db_user}"
  database_name       = "${var.db_name}"
  postgresql_version  = "10"
  common_tags         = "${var.common_tags}"
}

# region save DB details to Azure Key Vault
module "am-vault-api" {
  source = "git@github.com:hmcts/moj-module-key-vault?ref=master"
  name = "${var.raw_product}-${var.component}-${local.shortEnv}"
  product = "${var.product}-${var.component}"
  env = "${var.env}"
  tenant_id = "${var.tenant_id}"
  object_id = "${var.jenkins_AAD_objectId}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  product_group_object_id = "${var.product_group_object_id}"
}

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name      = "${var.product}-${var.component}-POSTGRES-USER"
  value     = "${module.postgres-am-api.user_name}"
  vault_uri = "${module.am-vault-api.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name      = "${var.product}-${var.component}-POSTGRES-PASS"
  value     = "${module.postgres-am-api.postgresql_password}"
  vault_uri = "${module.am-vault-api.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name      = "${var.product}-${var.component}-POSTGRES-HOST"
  value     = "${module.postgres-am-api.host_name}"
  vault_uri = "${module.am-vault-api.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name      = "${var.product}-${var.component}-POSTGRES-PORT"
  value     = "${module.postgres-am-api.postgresql_listen_port}"
  vault_uri = "${module.am-vault-api.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name      = "${var.product}-${var.component}-POSTGRES-DATABASE"
  value     = "${module.postgres-am-api.postgresql_database}"
  vault_uri = "${module.am-vault-api.key_vault_uri}"
}
# endregion

resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = "${var.location_app}"

  tags = "${merge(var.common_tags,
      map("lastUpdated", "${timestamp()}")
      )}"
}
