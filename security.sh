#!/usr/bin/env bash
echo ${TEST_URL}
zap-api-scan.py -t ${TEST_URL}/v2/api-docs -f openapi -u ${SECURITY_RULES} -P 1001 -l FAIL
cat zap.out
zap-cli --zap-url http://0.0.0.0 -p 1001 report -o /zap/api-report.html -f html
mkdir functional-output
cp /zap/api-report.html functional-output/
zap-cli -p 1001 alerts -l High
