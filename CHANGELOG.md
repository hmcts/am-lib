## [0.9.1](https://github.com/hmcts/am-lib/compare/0.9.0...0.9.1) (2019-05-21)



# [0.9.0](https://github.com/hmcts/am-lib/compare/0.8.0...0.9.0) (2019-05-21)


### Features

* enhanced filtering logic for security classification inheritance ([#180](https://github.com/hmcts/am-lib/issues/180)) ([279a889](https://github.com/hmcts/am-lib/commit/279a889))



# [0.8.0](https://github.com/hmcts/am-lib/compare/0.7.0...0.8.0) (2019-05-21)


### Bug Fixes

* AM-279 - Modified the jdk from oraclejdk to openjdk ([#158](https://github.com/hmcts/am-lib/issues/158)) ([2cdc9d7](https://github.com/hmcts/am-lib/commit/2cdc9d7))


### Features

* Filtering uses security classification(AM-77) ([#164](https://github.com/hmcts/am-lib/issues/164)) ([1dae741](https://github.com/hmcts/am-lib/commit/1dae741))
* retrieve security classifications of attributes for a role ([#142](https://github.com/hmcts/am-lib/issues/142)) ([1ad4a29](https://github.com/hmcts/am-lib/commit/1ad4a29))



# [0.7.0](https://github.com/hmcts/am-lib/compare/0.6.0...0.7.0) (2019-04-24)


### Bug Fixes

* make retrieving resource definitions with root create permissions use EXPLICIT and ROLE_BASED access type ([#136](https://github.com/hmcts/am-lib/issues/136)) ([5ef5c62](https://github.com/hmcts/am-lib/commit/5ef5c62))


### Features

* filter resource definitions with create permission using security classification (AM-253) ([#122](https://github.com/hmcts/am-lib/issues/122)) ([a5278c5](https://github.com/hmcts/am-lib/commit/a5278c5))
* revoke resource access based on relationship (AM-249) ([#130](https://github.com/hmcts/am-lib/issues/130)) ([18cfe13](https://github.com/hmcts/am-lib/commit/18cfe13))



# [0.6.0](https://github.com/hmcts/am-lib/compare/0.5.0...0.6.0) (2019-04-15)


### Features

* support relationship between user and resource in explicit access grants ([#112](https://github.com/hmcts/am-lib/issues/112)) ([5d87c15](https://github.com/hmcts/am-lib/commit/5d87c15))



# [0.5.0](https://github.com/hmcts/am-lib/compare/0.4.0...0.5.0) (2019-04-12)


### Features

* allow resource filtering for users with more than one role (AM-168) ([#102](https://github.com/hmcts/am-lib/issues/102)) ([2273ee6](https://github.com/hmcts/am-lib/commit/2273ee6))
* remove method that retrives list of accessors ([#114](https://github.com/hmcts/am-lib/issues/114)) ([d04d107](https://github.com/hmcts/am-lib/commit/d04d107))
* merge permissions in method that gets permissions for user roles (AM-60) ([#91](https://github.com/hmcts/am-lib/issues/91)) ([c4ae5d8](https://github.com/hmcts/am-lib/commit/c4ae5d8))
* add auditing of caller to grant / revoke methods (AM-213) ([#109](https://github.com/hmcts/am-lib/issues/109)) ([16556e8](https://github.com/hmcts/am-lib/commit/16556e8))



# [0.4.0](https://github.com/hmcts/am-lib/compare/0.3.0...0.4.0) (2019-03-26)


### Features

* add auditing of method calls (AM-213) ([#103](https://github.com/hmcts/am-lib/issues/103)) ([8cd63bd](https://github.com/hmcts/am-lib/commit/8cd63bd))
* add method to return resources with create permission (AM-221) ([#96](https://github.com/hmcts/am-lib/issues/96)) ([268b9be](https://github.com/hmcts/am-lib/commit/268b9be))
* add NONE value to security classification enum (AM-231) ([#94](https://github.com/hmcts/am-lib/issues/94)) ([1febec8](https://github.com/hmcts/am-lib/commit/1febec8))
* support granting explicit permissions of a resource to multiple users in a transaction (AM-218) ([#93](https://github.com/hmcts/am-lib/issues/93)) ([902e216](https://github.com/hmcts/am-lib/commit/902e216))



# [0.3.0](https://github.com/hmcts/am-lib/compare/0.2.0...0.3.0) (2019-03-11)


### Features

* alter revoking permissions to cascade on given attribute (AM-147) ([#80](https://github.com/hmcts/am-lib/issues/80)) ([eddb396](https://github.com/hmcts/am-lib/commit/eddb396))
* filter list of resources (AM-107) ([#79](https://github.com/hmcts/am-lib/issues/79)) ([64c6436](https://github.com/hmcts/am-lib/commit/64c6436))
* filter resource with role based access (AM-2) ([#77](https://github.com/hmcts/am-lib/issues/77)) ([c88456d](https://github.com/hmcts/am-lib/commit/c88456d))



# [0.2.0](https://github.com/hmcts/am-lib/compare/0.1.0...0.2.0) (2019-03-06)


### Features

* add JSON attribute filtering to existing method (AM-12) ([#68](https://github.com/hmcts/am-lib/issues/68)) ([505e505](https://github.com/hmcts/am-lib/commit/505e505))



# [0.1.0](https://github.com/hmcts/am-lib/compare/0.0.8...0.1.0) (2019-03-06)


### Features

* add API to retrieve permissions based on user role and resource definition (AM-135) ([#74](https://github.com/hmcts/am-lib/issues/74)) ([057c63a](https://github.com/hmcts/am-lib/commit/057c63a))
* add default role setup API (AM-59) ([#73](https://github.com/hmcts/am-lib/issues/73)) ([3b6c06a](https://github.com/hmcts/am-lib/commit/3b6c06a))
* add revoke explicit access method to am-lib (AM-65) ([#58](https://github.com/hmcts/am-lib/issues/58)) ([5dcd111](https://github.com/hmcts/am-lib/commit/5dcd111))
* allow granting access in transaction to more then one attribute at once (AM-99) ([#61](https://github.com/hmcts/am-lib/issues/61)) ([6110233](https://github.com/hmcts/am-lib/commit/6110233))
* change filtering result to an envelope with permissions map (AM-115) ([#53](https://github.com/hmcts/am-lib/issues/53)) ([2c18272](https://github.com/hmcts/am-lib/commit/2c18272))



