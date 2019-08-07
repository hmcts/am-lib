## [0.11.4](https://github.com/hmcts/am-lib/compare/0.10.4...0.11.4) (2019-08-07)


### Bug Fixes

* gatling fix initial data load Jenkins aat(Am-362) ([#245](https://github.com/hmcts/am-lib/issues/245)) ([9126500](https://github.com/hmcts/am-lib/commit/9126500))
* Gatling test initial data load ([#268](https://github.com/hmcts/am-lib/issues/268)) ([5463456](https://github.com/hmcts/am-lib/commit/5463456))
* initial test data load(AM-362) ([#249](https://github.com/hmcts/am-lib/issues/249)) ([cc99928](https://github.com/hmcts/am-lib/commit/cc99928))
* terraform module subscription ([#243](https://github.com/hmcts/am-lib/issues/243)) ([ce99a94](https://github.com/hmcts/am-lib/commit/ce99a94))
* Vulnerability Tomcat (Am 362 initial test data load) ([#247](https://github.com/hmcts/am-lib/issues/247)) ([93495f0](https://github.com/hmcts/am-lib/commit/93495f0))


### Features

* endpoint for returning resource accessors ([#252](https://github.com/hmcts/am-lib/issues/252)) ([5f88271](https://github.com/hmcts/am-lib/commit/5f88271))



## [0.10.4](https://github.com/hmcts/am-lib/compare/0.9.4...0.10.4) (2019-07-15)


### Bug Fixes

* add Role accessor type Am-326 ([#226](https://github.com/hmcts/am-lib/issues/226)) ([bed6457](https://github.com/hmcts/am-lib/commit/bed6457))
* Am 328 filter resource add resource type ([#224](https://github.com/hmcts/am-lib/issues/224)) ([b50da30](https://github.com/hmcts/am-lib/commit/b50da30))
* Am-309 Gatling Testing: Default data insertion ([#241](https://github.com/hmcts/am-lib/issues/241)) ([4121175](https://github.com/hmcts/am-lib/commit/4121175))
* AM-327 support for null relationship ([#225](https://github.com/hmcts/am-lib/issues/225)) ([b971923](https://github.com/hmcts/am-lib/commit/b971923))
* Api Refactor Create/Revoke and Filter Resource AM ([#205](https://github.com/hmcts/am-lib/issues/205)) ([16cc41d](https://github.com/hmcts/am-lib/commit/16cc41d))
* Aspect logging for user highest security classification in Filter resource api ([#201](https://github.com/hmcts/am-lib/issues/201)) ([0285062](https://github.com/hmcts/am-lib/commit/0285062))
* Log file changed to see gatling logs in Jenkins ([#193](https://github.com/hmcts/am-lib/issues/193)) ([d28612d](https://github.com/hmcts/am-lib/commit/d28612d))
* update invalid arguments provider ([#233](https://github.com/hmcts/am-lib/issues/233)) ([a295a98](https://github.com/hmcts/am-lib/commit/a295a98))


### Features

* AM-API Exception Handling and Swagger Support ([#200](https://github.com/hmcts/am-lib/issues/200)) ([547bcd2](https://github.com/hmcts/am-lib/commit/547bcd2))
* CCD to AM migration scripts (AM-266) ([#197](https://github.com/hmcts/am-lib/issues/197)) ([b0f607f](https://github.com/hmcts/am-lib/commit/b0f607f))
* error handling for db migration scripts (AM-308) ([#202](https://github.com/hmcts/am-lib/issues/202)) ([f1f56e4](https://github.com/hmcts/am-lib/commit/f1f56e4))
* optional security classification methods (AM-323) ([#227](https://github.com/hmcts/am-lib/issues/227)) ([767ebb0](https://github.com/hmcts/am-lib/commit/767ebb0))
* revoke access with optional resource name and service name ([#238](https://github.com/hmcts/am-lib/issues/238)) ([2459411](https://github.com/hmcts/am-lib/commit/2459411))



## [0.9.4](https://github.com/hmcts/am-lib/compare/0.9.3...0.9.4) (2019-05-23)


### Bug Fixes

* Gatling test case fix for Filter-Resource (AM-303) ([#192](https://github.com/hmcts/am-lib/issues/192)) ([59f092e](https://github.com/hmcts/am-lib/commit/59f092e))



## [0.9.3](https://github.com/hmcts/am-lib/compare/0.9.2...0.9.3) (2019-05-23)



## [0.9.2](https://github.com/hmcts/am-lib/compare/0.9.1...0.9.2) (2019-05-22)


### Bug Fixes

* update jackson-databind version ([#191](https://github.com/hmcts/am-lib/issues/191)) ([8394a2b](https://github.com/hmcts/am-lib/commit/8394a2b))



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



