package com.scr.project.sam.domains.actor.mapper

import org.bson.types.ObjectId

fun ObjectId?.toHexString() = this?.toHexString() ?: throw IllegalArgumentException("ObjectId is null")