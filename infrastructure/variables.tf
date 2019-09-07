variable "product" {
  type    = "string"
  default     = "am"
  description = "Access Management"
}

variable "component" {
  type = "string"
  default = "lib-test"
}

variable "location_app" {
  type    = "string"
  default = "UK South"
}

variable "env" {
  type = "string"
}

variable "ilbIp" {}

variable "subscription" {}

variable "capacity" {
  default = "1"
}

variable "instance_size" {
  default = "I1"
}

variable "common_tags" {
  type = "map"
}

variable "db_user" {
  type        = "string"
  default     = "amuser"
  description = "Database user name"
}

variable "db_name" {
  type        = "string"
  default     = "am"
  description = "Database name"
}

variable "raw_product" {
  default     = "am" // jenkins-library overrides product for PRs and adds e.g. pr-118-ccd
}

variable "tenant_id" {
  type                  = "string"
  description           = "(Required) The Azure Active Directory tenant ID that should be used for authenticating requests to the key vault. This is usually sourced from environemnt variables and not normally required to be specified."
}

variable "jenkins_AAD_objectId" {
  type                        = "string"
  description                 = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "product_group_object_id" {
  default = "3e235565-1054-4b83-86c1-318d05dd4342"
  description = "dcd_group_accessmanagement_v2"
}

variable "deployment_namespace" {
  type        = "string"
  default     = ""
}

variable "external_host_name" {
  default = ""
}

variable "asp_rg" {
  default = ""
}

variable "asp_name" {
  default = ""
}
