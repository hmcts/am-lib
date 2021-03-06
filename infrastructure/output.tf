output "sharedResourceGroup" {
  value = "${local.sharedResourceGroup}"
}

output "shared_vault_uri" {
  value = "${data.azurerm_key_vault.am_shared_vault.vault_uri}"
}
