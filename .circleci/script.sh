#! /bin/sh

PATH=$PATH:/tmp/buildevents/be/bin-linux:/tmp/be/bin-linux
BUILDEVENT_FILE=./${SERVICE}_specific_buildevents_file.txt

cat << 'EOF' >> ${BUILDEVENT_FILE}
ci.tests.build.completed=$(xpath -q -e 'failsafe-summary/completed/text()'  ${SERVICE}/target/failsafe-reports/failsafe-summary.xml)
ci.tests.build.errors=$(xpath -q -e 'failsafe-summary/errors/text()'  ${SERVICE}/target/failsafe-reports/failsafe-summary.xml)
ci.tests.build.failures=$(xpath -q -e 'failsafe-summary/failures/text()'  ${SERVICE}/target/failsafe-reports/failsafe-summary.xml)
ci.tests.build.skipped=$(xpath -q -e 'failsafe-summary/skipped/text()'  ${SERVICE}/target/failsafe-reports/failsafe-summary.xml)
EOF

buildevents cmd $CIRCLE_WORKFLOW_ID \
    $(cat /tmp/buildevents/be/${CIRCLE_JOB}-${CIRCLE_NODE_INDEX}/span_id) \
    ${SERVICE}-test-results -- echo "test results reported"
