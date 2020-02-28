package com.umair.beacons_plugin_example

data class Beacon(var identifier: String ?= "",
                  var uuid: String ?= "",
                  var major: Int = -1,
                  var minor: Int = -1,
                  var distance: Double,
                  var time: Long = 0L)