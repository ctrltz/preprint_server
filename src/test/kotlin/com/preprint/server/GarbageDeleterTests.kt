package com.preprint.server

import com.preprint.server.ref.CustomReferenceExtractor
import com.preprint.server.ref.custom.GarbageDeleter
import com.preprint.server.ref.custom.Line
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GarbageDeleterTests {

    fun loadFromResourses(fileName : String) : String {
        return this.javaClass.getResource("/garbageDeleterTests/$fileName").readText()
    }

    @Test
    fun removePageNumbersLowerTest() {
        val textLines = CustomReferenceExtractor.getLines(loadFromResourses("garbagePageNum1"))
        val expected = CustomReferenceExtractor.getLines(loadFromResourses("garbagePageNum1.exp"))
        val actual = GarbageDeleter.removePageNumbers(textLines)

        assertEquals(expected, actual)
    }

    @Test
    fun removePageNumbersUpperTest() {
        val textLines = CustomReferenceExtractor.getLines(loadFromResourses("garbagePageNum2"))
        val expected = CustomReferenceExtractor.getLines(loadFromResourses("garbagePageNum2.exp"))
        val actual = GarbageDeleter.removePageNumbers(textLines)

        assertEquals(expected, actual)
    }

    @Test
    fun removePageNumbersMixedTest() {
        val textLines = CustomReferenceExtractor.getLines(loadFromResourses("garbagePageNum3"))
        val expected = CustomReferenceExtractor.getLines(loadFromResourses("garbagePageNum3.exp"))
        val actual = GarbageDeleter.removePageNumbers(textLines)

        assertEquals(expected, actual)
    }

    @Test
    fun removePageNumbersFewPagesTest() {
        val textLines = CustomReferenceExtractor.getLines(loadFromResourses("garbagePageNum4"))
        val expected = CustomReferenceExtractor.getLines(loadFromResourses("garbagePageNum4.exp"))
        val actual = GarbageDeleter.removePageNumbers(textLines)

        assertEquals(expected, actual)
    }
}