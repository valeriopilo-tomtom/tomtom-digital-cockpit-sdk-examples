/*
 * Copyright © 2022 TomTom NV. All rights reserved.
 *
 * This software is the proprietary copyright of TomTom NV and its subsidiaries and may be
 * used for internal evaluation purposes or commercial use strictly subject to separate
 * license agreement between you and TomTom NV. If you are the licensee, you are only permitted
 * to use this software in accordance with the terms of your license agreement. If you are
 * not the licensee, you are not authorized to use this software in any manner and should
 * immediately return or destroy it.
 */

android {
    namespace = "com.quest.macaw.media.appservice"
    defaultConfig {
        minSdk = 30
        targetSdk = 30
        applicationId = "com.quest.macaw.media.appservice"
    }
}

dependencies {
    implementation(iviDependencies.androidxAppcompat)
    implementation(iviDependencies.androidxMedia)
    implementation(iviDependencies.utilGuava)
    implementation(iviDependencies.utilTraceEvents)
    implementation(iviDependencies.tomtomAutomotiveAndroidCar)

    implementation(files("libs/exoplayer-common.aar"))
    implementation(files("libs/exoplayer-core.aar"))
    implementation(files("libs/exoplayer-dash.aar"))
    implementation(files("libs/exoplayer-extractor.aar"))
    implementation(files("libs/exoplayer-ui.aar"))
    implementation(files("libs/exoplayer-hls.aar"))
    implementation(files("libs/extension-mediasession.aar"))
    implementation(files("libs/exoplayer-smoothstreaming.aar"))

    val room_version = "2.5.0"

    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

}
