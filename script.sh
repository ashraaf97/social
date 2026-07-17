#!/bin/sh
set -e

exec infisical run \
  --token "$INFISICAL_TOKEN" \
  --env "${INFISICAL_SECRET_ENV:-dev}" \
  --domain "${INFISICAL_API_URL:-https://app.infisical.com/api}" \
  -- java -jar app.jar