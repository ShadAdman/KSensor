package com.ksensor.sample

fun Double.toTwoDecimalString(): String {
    val integerPart = this.toLong()
    val decimalPart = ((this - integerPart) * 100).toLong()
    return "$integerPart.${decimalPart.toString().padStart(2, '0')}"
}
