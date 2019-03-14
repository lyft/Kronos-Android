publish:
	PUB_SUBJECT_NAME="lyft-org" \
	PUB_REPO_NAME="main" \
	PUB_PACKAGE_NAME="kronos" \
	PUB_VCS_URL="https://github.com/lyft/Kronos-Android" \
	./gradlew bintrayUpload --info
