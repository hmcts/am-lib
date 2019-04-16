#!/bin/bash

set -eE -u

# Release script does:
#   - calculate next version by looking at Git history
#   - cut release (subject to user approval) in the following steps:
#     - change version
#     - update changelog
#     - commit & push changes
#     - create tag & GitHub release
#
# The following Node.js packages are required:
#   - conventional-recommended-bump (provides conventional-recommended-bump command)
#   - conventional-changelog-cli (provides conventional-changelog command)
#   - conventional-github-releaser (provides conventional-github-releaser command)
#
# Above should be installed using either Yarn or NPM in global scope.

declare dir=$(dirname ${0})
declare preset=angular

#################
### Functions ###
#################

function validate_dependencies {
  if ! [[ -x "$(command -v conventional-recommended-bump)" ]]; then
    echo 'Error: conventional-recommended-bump is not installed' >&2
    exit 1
  fi
  if ! [[ -x "$(command -v conventional-changelog)" ]]; then
    echo 'Error: conventional-changelog is not installed' >&2
    exit 1
  fi
  if ! [[ -x "$(command -v conventional-github-releaser)" ]]; then
    echo 'Error: conventional-github-releaser is not installed' >&2
    exit 1
  fi
}

function validate_environment_variables {
  if [[ -z "${CONVENTIONAL_GITHUB_RELEASER_TOKEN:-}" ]]; then
    echo 'Error: CONVENTIONAL_GITHUB_RELEASER_TOKEN environment variable is not set' >&2
    exit 1
  fi
}

function get_current_version() {
  grep "version=" ${dir}/../gradle.properties | grep --extended-regexp --only-matching "([0-9]+\.[0-9]+\.[0-9]+)"
}

function calculate_next_version() {
  local current_version=${1}

  local -a next_version
  IFS="." read -ra next_version <<< "${current_version}"
  case $(conventional-recommended-bump --preset ${preset}) in
    major)
      next_version[0]=$((next_version[0] + 1))
      ;;
    minor)
      next_version[1]=$((next_version[1] + 1))
      ;;
    patch)
      next_version[2]=$((next_version[2] + 1))
      ;;
  esac

  echo "${next_version[0]}.${next_version[1]}.${next_version[2]}"
}

function change_version() {
  local current_version=${1}
  local next_version=${2}

  sed -i '' "s|version=${current_version}|version=${next_version}|" ${dir}/../gradle.properties
}

function update_changelog() {
  local next_version=${1}

  local context_file_path=${dir}/../build/conventional-changelog.context.json

  echo "{\"version\": \"${next_version}\"}" > ${context_file_path}
  conventional-changelog --preset ${preset} --context ${context_file_path} --infile ${dir}/../../CHANGELOG.md --same-file
  rm ${context_file_path}
}

function create_tag() {
  local next_version=${1}

  git commit --quiet --message "chore: bump version to ${next_version} and update CHANGELOG.md" ${dir}/../gradle.properties ${dir}/../../CHANGELOG.md
  git tag ${next_version}
  git push origin master --tags
}

function create_github_release() {
  conventional-github-releaser --preset ${preset}
}

function handle_unexpected {
  echo 'Release process failed! There is no automatic recovery - please start manual process with check of Git history' >&2
  exit 1
}

##############
### Script ###
##############

trap handle_unexpected ERR

validate_dependencies && validate_environment_variables

current_version=$(get_current_version)
next_version=$(calculate_next_version ${current_version})

read -p "Preparing release ${next_version} (bump from ${current_version}). Do you wish to continue (y/n)? " choice
case ${choice} in
  y|Y)
    change_version ${current_version} ${next_version}
    update_changelog ${next_version}
    create_tag ${next_version}
    create_github_release
    ;;
  n|N)
    echo 'Release process aborted'
    exit 0
    ;;
  *)
    echo 'Unrecognised option, try again...'
    ./$0 && exit 0
    ;;
esac
