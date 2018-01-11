package com.marknkamau.unipool.utils

import org.junit.Assert
import org.junit.Test

class UtilsKtTest {
    @Test
    fun containsNumber() {
        val test1 = "M4rk"
        val test2 = "Mark 123"
        val test3 = "Mark"

        Assert.assertEquals(true, test1.containsNumber())
        Assert.assertEquals(true, test2.containsNumber())
        Assert.assertEquals(false, test3.containsNumber())
    }

    @Test
    fun isRegNo(){
        val test1 = "KZY678F"
        val test2 = "kzy291z"
        val test3 = "kzy291fs"

        Assert.assertEquals(true, test1.isValidRegistrationNumber())
        Assert.assertEquals(true, test2.isValidRegistrationNumber())
        Assert.assertEquals(false, test3.isValidRegistrationNumber())
    }

    @Test
    fun shouldRunAsync(){
        val originalThread = Thread.currentThread()
        runAsync{
            val newThread = Thread.currentThread()
            Assert.assertNotEquals(originalThread, newThread)
        }
    }

}