def upload(sourceFile, destinationFolder, wipeFolder = false) {
    def downloadLink = ""

    if( ! ( env.DROPBOX_ACCESSTOKEN && env.DROPBOX_ACCESSTOKEN != "" ) ) {
        echo "NOTE: Dropbox uploader skipped due to missing Access Token."
        return downloadLink;
    }

    if( ! ( env.DROPBOX_UPLOADER_PATH && env.DROPBOX_UPLOADER_PATH != "" ) ) {
        echo "NOTE: Dropbox uploader path is not set."
        return downloadLink;
    }

    if( ! fileExists(env.DROPBOX_UPLOADER_PATH + '/dropbox_uploader.sh') ) {
        echo "NOTE: Dropbox uploader script does not exist."
        return downloadLink;
    }

    if( ! fileExists("${sourceFile}") ) {
        echo "NOTE: Dropbox uploader skipped due to source file ${sourceFile} not found."
        return downloadLink;
    }

    def db_cmd = "${env.DROPBOX_UPLOADER_PATH}/dropbox_uploader.sh -q -t \"${env.DROPBOX_ACCESSTOKEN}\"";

    def baseSourceName = sh (script: "basename ${sourceFile}", returnStdout: true).trim()

    if( wipeFolder ) {
        // make sure we have no macOS build folder on DropBox...
        try {
            sh "${db_cmd} delete \"${destinationFolder}\""
        } catch (err) {
            // echo "NOTE: Destination folder ${destinationFolder} did not exist."
        }

        // create new upload folder on DropBox
        sh "${db_cmd} mkdir \"${destinationFolder}\""
    }
    else {
        // make sure we have an upload folder on DropBox
        try {
            sh "${db_cmd} mkdir \"${destinationFolder}\""
        } catch (err) {
            // echo "NOTE: Destination folder ${destinationFolder}/macOS already exists."
        }
    }

    try {
        sh "${db_cmd} upload \"${sourceFile}\" \"${destinationFolder}/${baseSourceName}\""
        downloadLink = sh (script: "${db_cmd} share \"${destinationFolder}/${baseSourceName}\"", returnStdout: true).trim()
    } catch (err) {
        echo "ERROR: Dropbox uploader could not upload ${sourceFile} build to dropbox:/${destinationFolder}/${baseSourceName} and create a share link."
        downloadLink = ""
    }

    return downloadLink;
}

return this;
