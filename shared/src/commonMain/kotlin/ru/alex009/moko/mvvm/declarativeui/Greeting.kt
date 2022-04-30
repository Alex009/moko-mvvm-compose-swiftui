package ru.alex009.moko.mvvm.declarativeui

class Greeting {
    fun greeting(): String {
        return "Hello, ${Platform().platform}!"
    }
}