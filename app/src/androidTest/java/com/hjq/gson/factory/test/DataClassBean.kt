package com.hjq.gson.factory.test

import com.google.gson.FieldNamingPolicy
import com.google.gson.FieldNamingStrategy
import java.lang.reflect.Field

data class DataClassBean(
    var address: String? = "",
    var age: Int = 20,
    var alias: String,
    var child: DataClassChildBean,
    var company: String?,
    var interest: List<String>,
    var name: String = "轮子哥",
    @LowerCaseUnderScore var userDesc: String = "123",
    var stature: Int? = 180,
    var weight: Int
)

private object FieldNamingStrategy : FieldNamingStrategy {
    override fun translateName(field: Field): String {
        if (field.isAnnotationPresent(LowerCaseUnderScore::class.java)) {
            return FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES.translateName(field)
        }
        return FieldNamingPolicy.IDENTITY.translateName(field)
    }
}