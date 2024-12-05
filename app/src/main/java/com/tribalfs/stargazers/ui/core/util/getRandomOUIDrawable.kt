package com.tribalfs.stargazers.ui.core.util


import kotlin.random.Random

fun getRandomOUIDrawableId(): Int {
    val drawableClass = dev.oneuiproject.oneui.R.drawable::class.java
    val drawableFields = drawableClass.fields.filter { it.name.startsWith("ic_oui_weather") }
    val randomIndex = Random.nextInt(drawableFields.size)
    return drawableFields[randomIndex].getInt(null)
}

