package kr.open.library.simple_ui.core.unit.logcat.internal.stacktrace

import kr.open.library.simple_ui.core.logcat.internal.stacktrace.LogxStackTraceMetaData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for LogxStackTraceMetaData
 */
class LogxStackTraceMetaDataUnitTest {

    // ==============================================
    // Helper Methods
    // ==============================================

    private fun createStackTraceElement(
        className: String = "com.example.TestClass",
        methodName: String = "testMethod",
        fileName: String = "TestClass.kt",
        lineNumber: Int = 42
    ): StackTraceElement {
        return StackTraceElement(className, methodName, fileName, lineNumber)
    }

    // ==============================================
    // FileName Tests
    // ==============================================

    @Test
    fun fileName_returnsCorrectFileName_whenFileNameExists() {
        val element = createStackTraceElement(fileName = "MainActivity.kt")
        val metaData = LogxStackTraceMetaData(element)

        assertEquals("MainActivity.kt", metaData.fileName)
    }

    @Test
    fun fileName_usesProvidedFileName_whenAvailable() {
        val element = createStackTraceElement(
            className = "com.example.MyClass",
            fileName = "MyClass.kt"
        )
        val metaData = LogxStackTraceMetaData(element)

        assertEquals("MyClass.kt", metaData.fileName)
    }

    @Test
    fun fileName_handlesInnerClass_withValidFileName() {
        val element = createStackTraceElement(
            className = "com.example.OuterClass\$InnerClass",
            fileName = "OuterClass.kt"
        )
        val metaData = LogxStackTraceMetaData(element)

        assertEquals("OuterClass.kt", metaData.fileName)
    }

    @Test
    fun fileName_handlesComplexClassName() {
        val element = createStackTraceElement(
            className = "nonexistent.InvalidClass",
            fileName = "InvalidClass.java"
        )
        val metaData = LogxStackTraceMetaData(element)

        assertEquals("InvalidClass.java", metaData.fileName)
    }

    // ==============================================
    // Normal Message Format Tests
    // ==============================================

    @Test
    fun getMsgFrontNormal_returnsCorrectFormat() {
        val element = createStackTraceElement(
            fileName = "TestFile.kt",
            lineNumber = 123,
            methodName = "testFunction"
        )
        val metaData = LogxStackTraceMetaData(element)

        val result = metaData.getMsgFrontNormal()

        assertTrue(result.contains("TestFile.kt"))
        assertTrue(result.contains("123"))
        assertTrue(result.contains("testFunction"))
        assertTrue(result.contains("(TestFile.kt:123).testFunction - "))
    }

    @Test
    fun getMsgFrontNormal_includesFileLocationAndMethod() {
        val element = createStackTraceElement(
            fileName = "MainActivity.kt",
            lineNumber = 50,
            methodName = "onCreate"
        )
        val metaData = LogxStackTraceMetaData(element)

        val result = metaData.getMsgFrontNormal()

        assertEquals("(MainActivity.kt:50).onCreate - ", result)
    }

    @Test
    fun getMsgFrontNormal_cachesBehavior_byReturningConsistentResults() {
        val element = createStackTraceElement()
        val metaData = LogxStackTraceMetaData(element)

        val first = metaData.getMsgFrontNormal()
        val second = metaData.getMsgFrontNormal()

        assertEquals(first, second)
    }

    // ==============================================
    // Parent Message Format Tests
    // ==============================================

    @Test
    fun getMsgFrontParent_returnsCorrectFormat() {
        val element = createStackTraceElement(
            className = "com.example.MyClass",
            fileName = "MyClass.kt",
            lineNumber = 99,
            methodName = "parentMethod"
        )
        val metaData = LogxStackTraceMetaData(element)

        val result = metaData.getMsgFrontParent()

        assertTrue(result.contains("MyClass.kt"))
        assertTrue(result.contains("99"))
        assertTrue(result.contains("com.example.MyClass"))
        assertTrue(result.contains("parentMethod"))
        assertTrue(result.contains("(MyClass.kt:99) - [com.example.MyClass.parentMethod]"))
    }

    @Test
    fun getMsgFrontParent_includesFullClassNameAndMethod() {
        val element = createStackTraceElement(
            className = "org.test.SampleClass",
            fileName = "SampleClass.kt",
            lineNumber = 25,
            methodName = "execute"
        )
        val metaData = LogxStackTraceMetaData(element)

        val result = metaData.getMsgFrontParent()

        assertEquals("(SampleClass.kt:25) - [org.test.SampleClass.execute]", result)
    }

    @Test
    fun getMsgFrontParent_cachesBehavior_byReturningConsistentResults() {
        val element = createStackTraceElement()
        val metaData = LogxStackTraceMetaData(element)

        val first = metaData.getMsgFrontParent()
        val second = metaData.getMsgFrontParent()

        assertEquals(first, second)
    }

    // ==============================================
    // JSON Message Format Tests
    // ==============================================

    @Test
    fun getMsgFrontJson_returnsCorrectFormat() {
        val element = createStackTraceElement(
            fileName = "JsonHandler.kt",
            lineNumber = 77
        )
        val metaData = LogxStackTraceMetaData(element)

        val result = metaData.getMsgFrontJson()

        assertTrue(result.contains("JsonHandler.kt"))
        assertTrue(result.contains("77"))
        assertTrue(result.contains("(JsonHandler.kt:77) - "))
    }

    @Test
    fun getMsgFrontJson_includesOnlyFileLocationAndDash() {
        val element = createStackTraceElement(
            fileName = "Parser.kt",
            lineNumber = 200,
            methodName = "parse"
        )
        val metaData = LogxStackTraceMetaData(element)

        val result = metaData.getMsgFrontJson()

        assertEquals("(Parser.kt:200) - ", result)
        // JSON format should NOT include method name
        assertTrue(!result.contains("parse"))
    }

    @Test
    fun getMsgFrontJson_cachesBehavior_byReturningConsistentResults() {
        val element = createStackTraceElement()
        val metaData = LogxStackTraceMetaData(element)

        val first = metaData.getMsgFrontJson()
        val second = metaData.getMsgFrontJson()

        assertEquals(first, second)
    }

    // ==============================================
    // Format Comparison Tests
    // ==============================================

    @Test
    fun allFormats_produceDifferentResults() {
        val element = createStackTraceElement(
            className = "com.test.Example",
            fileName = "Example.kt",
            lineNumber = 10,
            methodName = "run"
        )
        val metaData = LogxStackTraceMetaData(element)

        val normal = metaData.getMsgFrontNormal()
        val parent = metaData.getMsgFrontParent()
        val json = metaData.getMsgFrontJson()

        // Normal format: (Example.kt:10).run -
        assertEquals("(Example.kt:10).run - ", normal)

        // Parent format: (Example.kt:10) - [com.test.Example.run]
        assertEquals("(Example.kt:10) - [com.test.Example.run]", parent)

        // JSON format: (Example.kt:10) -
        assertEquals("(Example.kt:10) - ", json)

        // All three should be different
        assertTrue(normal != parent)
        assertTrue(normal != json)
        assertTrue(parent != json)
    }

    @Test
    fun allFormats_shareCommonFileLocation() {
        val element = createStackTraceElement(
            fileName = "Shared.kt",
            lineNumber = 555
        )
        val metaData = LogxStackTraceMetaData(element)

        val normal = metaData.getMsgFrontNormal()
        val parent = metaData.getMsgFrontParent()
        val json = metaData.getMsgFrontJson()

        // All formats should contain the file location (Shared.kt:555)
        assertTrue(normal.contains("(Shared.kt:555)"))
        assertTrue(parent.contains("(Shared.kt:555)"))
        assertTrue(json.contains("(Shared.kt:555)"))
    }

    // ==============================================
    // Edge Cases and Special Scenarios
    // ==============================================

    @Test
    fun metaData_handlesNegativeLineNumber() {
        val element = StackTraceElement(
            "com.example.Test",
            "method",
            "Test.kt",
            -1 // Native method has -1
        )
        val metaData = LogxStackTraceMetaData(element)

        val result = metaData.getMsgFrontNormal()

        assertTrue(result.contains("-1"))
    }

    @Test
    fun metaData_handlesZeroLineNumber() {
        val element = createStackTraceElement(lineNumber = 0)
        val metaData = LogxStackTraceMetaData(element)

        val result = metaData.getMsgFrontJson()

        assertTrue(result.contains(":0)"))
    }

    @Test
    fun metaData_handlesLongClassName() {
        val longClassName = "com.example.very.long.package.name.structure.MyVeryLongClassName"
        val element = createStackTraceElement(className = longClassName)
        val metaData = LogxStackTraceMetaData(element)

        val result = metaData.getMsgFrontParent()

        assertTrue(result.contains(longClassName))
    }

    @Test
    fun metaData_handlesLongMethodName() {
        val longMethodName = "thisIsAVeryLongMethodNameThatShouldStillWork"
        val element = createStackTraceElement(methodName = longMethodName)
        val metaData = LogxStackTraceMetaData(element)

        val result = metaData.getMsgFrontNormal()

        assertTrue(result.contains(longMethodName))
    }

    // ==============================================
    // Data Class Behavior Tests
    // ==============================================

    @Test
    fun dataClass_equality_sameElementProducesSameMetaData() {
        val element = createStackTraceElement()
        val metaData1 = LogxStackTraceMetaData(element)
        val metaData2 = LogxStackTraceMetaData(element)

        assertEquals(metaData1, metaData2)
    }

    @Test
    fun dataClass_hashCode_sameElementProducesSameHashCode() {
        val element = createStackTraceElement()
        val metaData1 = LogxStackTraceMetaData(element)
        val metaData2 = LogxStackTraceMetaData(element)

        assertEquals(metaData1.hashCode(), metaData2.hashCode())
    }

    @Test
    fun dataClass_toString_returnsValidString() {
        val element = createStackTraceElement()
        val metaData = LogxStackTraceMetaData(element)

        val result = metaData.toString()

        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }
}
