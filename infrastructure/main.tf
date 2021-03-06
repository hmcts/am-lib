provider "azurerm" {}

locals {
  app_full_name = "${var.product}-${var.component}"
  ase_name = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
  envInUse = "${(var.env == "preview" || var.env == "spreview") ? "aat" : var.env}"
  shortEnv = "${(var.env == "preview" || var.env == "spreview") ? var.deployment_namespace : var.env}"

  previewVaultName = "${var.raw_product}-aat"
  nonPreviewVaultName = "${var.raw_product}-${var.env}"
  vaultName = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"

  previewResourceGroup = "${var.raw_product}-shared-infrastructure-aat"
  nonPreviewResourceGroup = "${var.raw_product}-shared-infrastructure-${var.env}"
  sharedResourceGroup = "${(var.env == "preview" || var.env == "spreview") ? local.previewResourceGroup : local.nonPreviewResourceGroup}"

  sharedAspName = "${var.raw_product}-${local.envInUse}"
  sharedAspRg = "${var.raw_product}-shared-infrastructure-${local.envInUse}"
  asp_name = "${(var.env == "preview" || var.env == "spreview") ? "null" : local.sharedAspName}"
  asp_rg = "${(var.env == "preview" || var.env == "spreview") ? "null" : local.sharedAspRg}"

  s2s_url = "http://rpe-service-auth-provider-${local.envInUse}.service.core-compute-${local.envInUse}.internal"
  s2s_vault_name = "s2s-${local.envInUse}"
  s2s_vault_uri = "https://s2s-${local.envInUse}.vault.azure.net/"
}

module "am-accessmgmt-api" {
  source              = "git@github.com:hmcts/cnp-module-webapp?ref=master"
  product             = "${local.app_full_name}"
  location            = "${var.location_app}"
  env                 = "${var.env}"
  ilbIp               = "${var.ilbIp}"
  subscription        = "${var.subscription}"
  is_frontend         = "${var.external_host_name != "" ? "1" : "0"}"
  additional_host_name = "${var.external_host_name != "" ? var.external_host_name : "null"}"
  capacity            = "${var.capacity}"
  instance_size       = "${var.instance_size}"
  asp_rg              = "${local.asp_rg}"
  asp_name            = "${local.asp_name}"
  common_tags         = "${var.common_tags}"

  app_settings = {
    LOGBACK_REQUIRE_ALERT_LEVEL = "false"
    LOGBACK_REQUIRE_ERROR_CODE  = "false"

    AM_DB_HOST = "${module.postgres-am-api.host_name}"
    AM_DB_PORT = "${module.postgres-am-api.postgresql_listen_port}"
    AM_DB_NAME = "${module.postgres-am-api.postgresql_database}"
    AM_DB_USERNAME = "${module.postgres-am-api.user_name}"
    AM_DB_PASSWORD = "${module.postgres-am-api.postgresql_password}"
    AM_DB_PARAMS = "?sslmode=require"
    S2S_URL = "${local.s2s_url}"
  }
}

module "postgres-am-api" {
  source              = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product             = "${local.app_full_name}"
  env                 = "${var.env}"
  location            = "${var.location_app}"
  postgresql_user     = "${var.db_user}"
  database_name       = "${var.db_name}"
  postgresql_version  = "10"
  common_tags         = "${var.common_tags}"
  subscription        = "${var.subscription}"
}

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name = "${var.component}-POSTGRES-USER"
  value = "${module.postgres-am-api.user_name}"
  key_vault_id = "${data.azurerm_key_vault.am_shared_vault.id}"
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name = "${var.component}-POSTGRES-PASS"
  value = "${module.postgres-am-api.postgresql_password}"
  key_vault_id = "${data.azurerm_key_vault.am_shared_vault.id}"
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name = "${var.component}-POSTGRES-HOST"
  value = "${module.postgres-am-api.host_name}"
  key_vault_id = "${data.azurerm_key_vault.am_shared_vault.id}"
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name = "${var.component}-POSTGRES-PORT"
  value = "${module.postgres-am-api.postgresql_listen_port}"
  key_vault_id = "${data.azurerm_key_vault.am_shared_vault.id}"
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name = "${var.component}-POSTGRES-DATABASE"
  value = "${module.postgres-am-api.postgresql_database}"
  key_vault_id = "${data.azurerm_key_vault.am_shared_vault.id}"
}

resource "azurerm_key_vault_secret" "S2S_SECRET" {
  name = "s2s-secret"
  value = "${data.azurerm_key_vault_secret.s2s_secret.value}"
  key_vault_id = "${data.azurerm_key_vault.am_shared_vault.id}"
}

resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = "${var.location_app}"

  tags = "${merge(var.common_tags,
      map("lastUpdated", "${timestamp()}")
      )}"
}

data "azurerm_key_vault" "am_shared_vault" {
  name = "${local.vaultName}"
  resource_group_name = "${local.sharedResourceGroup}"
}

data "azurerm_key_vault" "s2s_key_vault" {
  name = "s2s-${local.envInUse}"
  resource_group_name = "rpe-service-auth-provider-${local.envInUse}"
}

data "azurerm_key_vault_secret" "s2s_secret" {
  name = "microservicekey-am-accessmgmt-api"
  key_vault_id = "${data.azurerm_key_vault.s2s_key_vault.id}"
}
