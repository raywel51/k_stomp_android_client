# Android STOMP WebSocket Client(KStomp)

[![Release](https://jitpack.io/v/raywel51/k_stomp_android_client.svg)](https://jitpack.io/#raywel51/k_stomp_android_client)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android-green.svg)]()
[![Language](https://img.shields.io/badge/language-Kotlin-blue.svg)]()

## Overview

This library provide support for STOMP protocol https://stomp.github.io/
At now library works only as client for backend with support STOMP, such as
NodeJS (stompjs or other) or Spring Boot (SockJS).

A lightweight Android library that provides a **STOMP client over WebSocket**.

This library is designed to connect with STOMP-enabled backends such as **Spring Boot**, **NodeJS**, or any STOMP-compatible broker.

## Features

✅ STOMP over WebSocket client  
✅ Compatible with Spring Boot WebSocket endpoints (SockJS optional)  
✅ Subscribe to topics and receive messages  
✅ Send messages to destinations  
✅ Connection lifecycle events (OPENED / ERROR / CLOSED)  
✅ Supports multiple WebSocket providers

---

## Installation (Gradle)

Add JitPack repository:

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

dependencies {
    implementation("com.github.raywel51:k_stomp_android_client:v0.1.1")
}
```