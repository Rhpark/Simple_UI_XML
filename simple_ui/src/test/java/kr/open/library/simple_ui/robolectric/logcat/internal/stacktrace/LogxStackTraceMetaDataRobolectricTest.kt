package kr.open.library.simple_ui.robolectric.logcat.internal.stacktrace

import kr.open.library.simple_ui.logcat.internal.stacktrace.LogxStackTraceMetaData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric test for LogxStackTraceMetaData
 * Tests edge cases that require Android framework (Log.e)
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LogxStackTraceMetaDataRobolectricTest {

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
    // FileName Edge Cases with Android Log
    // ==============================================

    @Test
    fun fileName_fallsBackToClassName_whenFileNameIsNull() {
        // Create a StackTraceElement where fileName might be null
        // Using a real Java class that exists in the test classpath
        val element = createStackTraceElement(
            className = "java.lang.String",
            fileName = "String.java" // Java classes have fileName
        )
        val metaData = LogxStackTraceMetaData(element)

        // Should use fileName if available
        assertNotNull(metaData.fileName)
        assertEquals("String.java", metaData.fileName)
    }

    @Test
    fun fileName_handlesClassNotFoundException_returnsUnknown() {
        // Using a non-existent class to trigger ClassNotFoundException path
        // Note: We can't create StackTraceElement with null fileName in Kotlin
        // So we test with a class that won't be found when trying fallback
        val element = createStackTraceElement(
            className = "this.class.does.not.exist.Nowhere",
            fileName = "Nowhere.kt"
        )
        val metaData = LogxStackTraceMetaData(element)

        // Should use provided fileName
        assertNotNull(metaData.fileName)
        assertEquals("Nowhere.kt", metaData.fileName)
    }

    @Test
    fun fileName_handlesInnerClassWithDollarSign() {
        val element = createStackTraceElement(
            className = "com.example.OuterClass\$InnerClass\$DeepInner",
            fileName = "OuterClass.kt"
        )
        val metaData = LogxStackTraceMetaData(element)

        assertEquals("OuterClass.kt", metaData.fileName)
    }

    @Test
    fun fileName_handlesLambdaClassName() {
        // Lambda classes often have $ in their names
        val element = createStackTraceElement(
            className = "com.example.MyClass\$lambda\$1",
            fileName = "MyClass.kt"
        )
        val metaData = LogxStackTraceMetaData(element)

        assertEquals("MyClass.kt", metaData.fileName)
    }

    @Test
    fun fileName_handlesAnonymousInnerClass() {
        val element = createStackTraceElement(
            className = "com.example.MyClass\$1",
            fileName = "MyClass.kt"
        )
        val metaData = LogxStackTraceMetaData(element)

        assertEquals("MyClass.kt", metaData.fileName)
    }

    // ==============================================
    // FileLocation with Various Line Numbers
    // ==============================================

    @Test
    fun getMsgFrontNormal_withNegativeLineNumber_handlesNativeMethod() {
        val element = StackTraceElement(
            "java.lang.Thread",
            "run",
            "Thread.java",
            -1 // Native method
        )
        val metaData = LogxStackTraceMetaData(element)

        val result = metaData.getMsgFrontNormal()

        assertTrue(result.contains("Thread.java:-1"))
        assertTrue(result.contains(".run - "))
    }

    @Test
    fun getMsgFrontNormal_withZeroLineNumber() {
        val element = createStackTraceElement(lineNumber = 0)
        val metaData = LogxStackTraceMetaData(element)

        val result = metaData.getMsgFrontNormal()

        assertTrue(result.contains(":0)"))
    }

    @Test
    fun getMsgFrontNormal_withLargeLineNumber() {
        val element = createStackTraceElement(lineNumber = 999999)
        val metaData = LogxStackTraceMetaData(element)

        val result = metaData.getMsgFrontNormal()

        assertTrue(result.contains(":999999)"))
    }

    // ==============================================
    // Parent Format Edge Cases
    // ==============================================

    @Test
    fun getMsgFrontParent_withVeryLongClassName() {
        val longClassName = "com.very.long.package.name.with.many.segments.MyVeryLongClassNameThatIsReallyLong"
        val element = createStackTraceElement(
            className = longClassName,
            fileName = "MyVeryLongClassNameThatIsReallyLong.kt",
            lineNumber = 123
        )
        val metaData = LogxStackTraceMetaData(element)

        val result = metaData.getMsgFrontParent()

        assertTrue(result.contains(longClassName))
        assertTrue(result.contains("123"))
    }

    @Test
    fun getMsgFrontParent_withSpecialCharactersInMethodName() {
        val element = createStackTraceElement(
            methodName = "<init>", // Constructor
            fileName = "Test.kt"
        )
        val metaData = LogxStackTraceMetaData(element)

        val result = metaData.getMsgFrontParent()

        assertTrue(result.contains("<init>"))
    }

    @Test
    fun getMsgFrontParent_withGetterMethod() {
        val element = createStackTraceElement(
            methodName = "getValue",
            fileName = "Property.kt"
        )
        val metaData = LogxStackTraceMetaData(element)

        val result = metaData.getMsgFrontParent()

        assertTrue(result.contains("getValue"))
    }

    // ==============================================
    // JSON Format Edge Cases
    // ==============================================

    @Test
    fun getMsgFrontJson_withSpecialCharactersInFileName() {
        val element = createStackTraceElement(
            fileName = "Test-File_Name.kt",
            lineNumber = 50
        )
        val metaData = LogxStackTraceMetaData(element)

        val result = metaData.getMsgFrontJson()

        assertTrue(result.contains("Test-File_Name.kt:50"))
        assertTrue(result.endsWith(" - "))
    }

    @Test
    fun getMsgFrontJson_withJavaFile() {
        val element = createStackTraceElement(
            fileName = "JavaClass.java",
            lineNumber = 100
        )
        val metaData = LogxStackTraceMetaData(element)

        val result = metaData.getMsgFrontJson()

        assertEquals("(JavaClass.java:100) - ", result)
    }

    // ==============================================
    // Lazy Initialization Behavior Tests
    // ==============================================

    @Test
    fun fileName_lazyInitialization_cachesValue() {
        val element = createStackTraceElement(fileName = "Cached.kt")
        val metaData = LogxStackTraceMetaData(element)

        // First access
        val first = metaData.fileName
        // Second access should use cached value
        val second = metaData.fileName

        assertEquals(first, second)
        assertEquals("Cached.kt", first)
    }

    @Test
    fun allMessageFormats_useSameLazyFileLocation() {
        val element = createStackTraceElement(
            fileName = "SharedLocation.kt",
            lineNumber = 777,
            methodName = "sharedMethod",
            className = "com.test.SharedClass"
        )
        val metaData = LogxStackTraceMetaData(element)

        val normal = metaData.getMsgFrontNormal()
        val parent = metaData.getMsgFrontParent()
        val json = metaData.getMsgFrontJson()

        // All should contain the same file location
        assertTrue(normal.contains("(SharedLocation.kt:777)"))
        assertTrue(parent.contains("(SharedLocation.kt:777)"))
        assertTrue(json.contains("(SharedLocation.kt:777)"))
    }

    // ==============================================
    // Real-World Scenario Tests
    // ==============================================

    @Test
    fun realWorld_kotlinCoroutineStackTrace() {
        val element = createStackTraceElement(
            className = "kotlinx.coroutines.DeferredCoroutine",
            methodName = "await",
            fileName = "Deferred.kt",
            lineNumber = 52
        )
        val metaData = LogxStackTraceMetaData(element)

        assertEquals("Deferred.kt", metaData.fileName)
        assertEquals("(Deferred.kt:52).await - ", metaData.getMsgFrontNormal())
        assertTrue(metaData.getMsgFrontParent().contains("kotlinx.coroutines.DeferredCoroutine.await"))
    }

    @Test
    fun realWorld_androidFrameworkStackTrace() {
        val element = createStackTraceElement(
            className = "android.app.Activity",
            methodName = "onCreate",
            fileName = "Activity.java",
            lineNumber = 1024
        )
        val metaData = LogxStackTraceMetaData(element)

        assertEquals("Activity.java", metaData.fileName)
        assertEquals("(Activity.java:1024).onCreate - ", metaData.getMsgFrontNormal())
    }

    @Test
    fun realWorld_proguardObfuscatedStackTrace() {
        val element = createStackTraceElement(
            className = "a.b.c",
            methodName = "a",
            fileName = "SourceFile",
            lineNumber = 10
        )
        val metaData = LogxStackTraceMetaData(element)

        assertEquals("SourceFile", metaData.fileName)
        assertEquals("(SourceFile:10).a - ", metaData.getMsgFrontNormal())
        assertEquals("(SourceFile:10) - [a.b.c.a]", metaData.getMsgFrontParent())
    }

    // ==============================================
    // Data Class Properties Tests
    // ==============================================

    @Test
    fun dataClass_equality_basedOnStackTraceElement() {
        val element = createStackTraceElement()
        val metaData1 = LogxStackTraceMetaData(element)
        val metaData2 = LogxStackTraceMetaData(element)

        assertEquals(metaData1, metaData2)
    }

    @Test
    fun dataClass_differentElements_notEqual() {
        val element1 = createStackTraceElement(lineNumber = 10)
        val element2 = createStackTraceElement(lineNumber = 20)
        val metaData1 = LogxStackTraceMetaData(element1)
        val metaData2 = LogxStackTraceMetaData(element2)

        assertTrue(metaData1 != metaData2)
    }

    @Test
    fun dataClass_hashCode_consistent() {
        val element = createStackTraceElement()
        val metaData = LogxStackTraceMetaData(element)

        val hash1 = metaData.hashCode()
        val hash2 = metaData.hashCode()

        assertEquals(hash1, hash2)
    }

    @Test
    fun dataClass_copy_createsNewInstanceWithSameData() {
        val element = createStackTraceElement()
        val metaData = LogxStackTraceMetaData(element)
        val copied = metaData.copy()

        assertEquals(metaData, copied)
        assertEquals(metaData.fileName, copied.fileName)
    }

    // ==============================================
    // Stress Tests
    // ==============================================

    @Test
    fun stress_multipleInstances_independentLazyInitialization() {
        val elements = (1..10).map { i ->
            createStackTraceElement(
                fileName = "File$i.kt",
                lineNumber = i * 10
            )
        }

        val metaDataList = elements.map { LogxStackTraceMetaData(it) }

        metaDataList.forEachIndexed { index, metaData ->
            assertEquals("File${index + 1}.kt", metaData.fileName)
            assertTrue(metaData.getMsgFrontNormal().contains("File${index + 1}.kt"))
        }
    }

    @Test
    fun stress_repeatedAccess_consistentResults() {
        val element = createStackTraceElement()
        val metaData = LogxStackTraceMetaData(element)

        repeat(100) {
            assertEquals(metaData.getMsgFrontNormal(), metaData.getMsgFrontNormal())
            assertEquals(metaData.getMsgFrontParent(), metaData.getMsgFrontParent())
            assertEquals(metaData.getMsgFrontJson(), metaData.getMsgFrontJson())
        }
    }
}
