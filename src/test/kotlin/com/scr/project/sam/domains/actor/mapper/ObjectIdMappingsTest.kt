package com.scr.project.sam.domains.actor.mapper

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test

class ObjectIdMappingsTest {

    @Test
    fun `toHexString should succeed when objectId is not null`() {
        val objectId: ObjectId? = ObjectId("507f1f77bcf86cd799439011")
        val hexString = objectId.toHexString()
        assertThat(hexString).isEqualTo("507f1f77bcf86cd799439011")
    }

    @Test
    fun `toHexString should return exception when objectId is null`() {
        val objectId: ObjectId? = null
        val throwable = catchThrowable { objectId.toHexString() }
        assertThat(throwable).isInstanceOf(IllegalArgumentException::class.java)
    }
}