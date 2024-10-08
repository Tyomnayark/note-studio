package com.tyom.core_utils.extensions

fun List<Any>.ifNotEmpty(func: () -> Unit) =
    if (this.isNotEmpty()) {
        func()
    } else {
        Unit
    }

fun List<Any>.ifEmpty(func: () -> Unit) =
    if (this.isEmpty()) {
        func()
    } else {
        Unit
    }