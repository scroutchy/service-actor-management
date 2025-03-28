package com.scr.project.sam

enum class SchemaCompatibilityLevel {
    /** FORWARD compatibility with all previous versions */
    FORWARD_TRANSITIVE,

    /** No compatibility with previous major versions */
    NONE
}