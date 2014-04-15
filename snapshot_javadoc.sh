#!/bin/sh

ARTIFACT_ID="executor"

if [ $# -ne 1 ]; then
	echo "Usage: $0 output_dir"
	exit 1
fi

WWW=$1

echo "Publishing javadoc into ${WWW}"
mkdir -p ${WWW}

#sub-modules apidoc
echo "Generate the javadoc for the modules"
mvn javadoc:javadoc > /dev/null
mvn javadoc:jar > /dev/null
mkdir -p ${WWW}/${ARTIFACT_ID} > /dev/null
rm -rf ${WWW}/${ARTIFACT_ID}/apidocs-snapshot
mv ${ARTIFACT_ID}/target/apidocs ${WWW}/${ARTIFACT_ID}/apidocs-snapshot