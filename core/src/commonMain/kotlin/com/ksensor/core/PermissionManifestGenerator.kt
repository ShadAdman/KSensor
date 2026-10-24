package com.ksensor.core

/**
 * Pure generator utilities for producing AndroidManifest.xml and iOS Info.plist permission entries.
 */
object PermissionManifestGenerator {

    /**
     * Generates AndroidManifest.xml uses-permission tags from a list of [PermissionMetadata].
     *
     * @param metadataList List of permission metadata entries.
     * @param includeOptional Whether to include permissions marked as optional (isRequired = false).
     * @param includeComments Whether to include XML comments in the snippet.
     */
    fun generateAndroidManifest(
        metadataList: List<PermissionMetadata>,
        includeOptional: Boolean = true,
        includeComments: Boolean = true
    ): String {
        val filtered = metadataList.filter {
            (it.platform == TargetPlatform.COMMON || it.platform == TargetPlatform.ANDROID) &&
                    (includeOptional || it.isRequired)
        }

        val uniquePermissions = filtered.distinctBy { it.permissionName }

        if (uniquePermissions.isEmpty()) {
            return if (includeComments) "<!-- No Android permissions required -->" else ""
        }

        val builder = StringBuilder()
        if (includeComments) {
            builder.appendLine("<!-- AndroidManifest.xml permissions for KSensor plugins -->")
        }

        uniquePermissions.forEach { meta ->
            builder.append("  <uses-permission android:name=\"${meta.permissionName}\"")
            if (meta.minSdkVersion != null) {
                builder.append(" android:minSdkVersion=\"${meta.minSdkVersion}\"")
            }
            if (meta.maxSdkVersion != null) {
                builder.append(" android:maxSdkVersion=\"${meta.maxSdkVersion}\"")
            }
            meta.extraAttributes.forEach { (key, value) ->
                builder.append(" android:$key=\"$value\"")
            }
            builder.appendLine(" />")
        }

        return builder.toString().trimEnd()
    }

    /**
     * Generates iOS Info.plist key-value entries for usage descriptions from a list of [PermissionMetadata].
     *
     * @param metadataList List of permission metadata entries.
     * @param includeOptional Whether to include permissions marked as optional (isRequired = false).
     * @param includeComments Whether to include XML comments in the snippet.
     */
    fun generateIosInfoPlist(
        metadataList: List<PermissionMetadata>,
        includeOptional: Boolean = true,
        includeComments: Boolean = true
    ): String {
        val filtered = metadataList.filter {
            (it.platform == TargetPlatform.COMMON || it.platform == TargetPlatform.IOS) &&
                    (includeOptional || it.isRequired) &&
                    !it.usageDescriptionKey.isNullOrBlank()
        }

        val uniquePermissions = filtered.distinctBy { it.usageDescriptionKey }

        if (uniquePermissions.isEmpty()) {
            return if (includeComments) "<!-- No iOS Info.plist permission entries required -->" else ""
        }

        val builder = StringBuilder()
        if (includeComments) {
            builder.appendLine("<!-- Info.plist permission entries for KSensor plugins -->")
        }

        uniquePermissions.forEach { meta ->
            val key = meta.usageDescriptionKey ?: meta.permissionName
            val value = meta.usageDescriptionDefault ?: "This app requires access to $key."
            builder.appendLine("  <key>$key</key>")
            builder.appendLine("  <string>$value</string>")
        }

        return builder.toString().trimEnd()
    }
}
