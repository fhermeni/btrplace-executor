#!/bin/sh
#Script to notify the website about a release

function sedInPlace {
	if [ $(uname) = "Darwin" ]; then			
		sed -i '' "$1" $2
	else
		sed -i'' "$1" $2
	fi
}

if [ $# -ne 2 ]; then
    echo "Usage: $0 [site|code] version_number"
    echo "'site': notify a release on the website"
    echo "'code': upgrade the version number in the code"
    exit 1
fi
VERSION=$2
REPO_URL="http://btrp.inria.fr/repos"
APIDOC_URL="http://btrp.inria.fr/apidocs"

case $1	in

site)	
	WWW_HOOK="http://btrp.inria.fr/admin/bump_release.php"

	JSON="{\"version\":\"$VERSION\",\	
	\"title\":\"executor\",\
	\"apidoc\":\"$APIDOC_URL/releases/btrplace/executor/$VERSION/\",\
	\"changelog\":\"https://github.com/fhermeni/btrplace-executor/tree/btrplace-executor-$VERSION/CHANGES.md\",\
	\"binary\":\"$REPO_URL/releases/btrplace/btrplace-executor/$VERSION/btrplace-executor-$VERSION.jar\",\
	\"sources\":\"https://github.com/fhermeni/btrplace-executor/tree/btrplace-executor-$VERSION\"
	}"
	curl -X POST --data "data=$JSON" $WWW_HOOK
	;;
code)

	## The README.md
	# Update of the version number for maven usage	
		
	sedInPlace "s%<version>.*</version>%<version>$VERSION</version>%"  README.md
	
	snapshot=0
	echo $VERSION | grep "\-SNAPSHOT$" > /dev/null && snapshot=1

	if [ $snapshot = 0 ]; then 
		# Update the bundle and the apidoc location
		sedInPlace "s%$APIDOC_URL/.*%$APIDOC_URL/releases/btrplace/executor/$VERSION/%" README.md
	else 
		# Update the bundle and the apidoc location
		sedInPlace "s%$APIDOC_URL/.*%$APIDOC_URL/snapshots/btrplace/executor/%" README.md
	fi

	## The CHANGES.md file
	d=`LANG=en_US.utf8 date +"%d %b %Y"`
	REGEX="s%????*%${VERSION} - ${d}%"	
	sedInPlace "${REGEX}" CHANGES.md
	;;

*)
		echo "Target must be either 'site' or 'code'"
		exit 1
esac

