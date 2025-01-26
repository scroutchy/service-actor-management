package com.scr.project.sam

import org.junit.jupiter.api.ClassDescriptor
import org.junit.jupiter.api.ClassOrderer
import org.junit.jupiter.api.ClassOrdererContext
import org.springframework.boot.test.context.SpringBootTest

class TestClassOrderer: ClassOrderer {

    override fun orderClasses(context: ClassOrdererContext?) {
        context?.classDescriptors?.sortBy { weight(it) }
    }

    private fun weight(descriptor: ClassDescriptor): Int {

        return when {
            descriptor.isAnnotated(SpringBootTest::class.java) -> 2
            else -> 1
        }
    }
}