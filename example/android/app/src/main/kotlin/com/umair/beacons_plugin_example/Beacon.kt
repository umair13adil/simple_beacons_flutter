package com.umair.beacons_plugin_example

data class Beacon(
        var name: String? = "",
        var address: String? = "",
        var identifier: String? = "",
        var uuid: String? = "",
        var major: String = "",
        var minor: String = "",
        var distance: String = "",
        var time: Long = 0L)