#! /bin/sh

false

status_code=$?

BUILDEVENT_FILE=./${SERVICE}_specific_buildevents_file.txt

cat << 'EOF' >> ${BUILDEVENT_FILE}
ci.tests.build.completed=$(xpath -q -e 'failsafe-summary/completed/text()'  ${SERVICE}/target/failsafe-reports/failsafe-summary.xml)
ci.tests.build.errors=$(xpath -q -e 'failsafe-summary/errors/text()'  ${SERVICE}/target/failsafe-reports/failsafe-summary.xml)
ci.tests.build.failures=$(xpath -q -e 'failsafe-summary/failures/text()'  ${SERVICE}/target/failsafe-reports/failsafe-summary.xml)
ci.tests.build.skipped=$(xpath -q -e 'failsafe-summary/skipped/text()'  ${SERVICE}/target/failsafe-reports/failsafe-summary.xml)
EOF
