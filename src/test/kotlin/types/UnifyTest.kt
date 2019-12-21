package types

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import syntax.Monotype
import syntax.Parser

@Tag("unify")
internal class UnifyTest {
    private var typeChecker = TypeChecker(CheckState())

    @BeforeEach
    fun setUp() {
        typeChecker.checkState = CheckState()
    }

    @Test
    fun `unifying matching primitive types should succeed`() {
        "Int" unifiedWith "Int"
        "String" unifiedWith "String"
        "Bool" unifiedWith "Bool"
    }

    @Test
    fun `unifying non-matching primitive types should fail`() {
        failsWith(UnificationFailure("Int", "String")) { "Int" unifiedWith "String" }
        failsWith(UnificationFailure("String", "Bool")) { "String" unifiedWith "Bool" }
        failsWith(UnificationFailure("Int", "Bool")) { "Int" unifiedWith "Bool" }
        failsWith(UnificationFailure("Int -> Int", "Bool")) { "Int -> Int" unifiedWith "Bool" }
    }

    @Test
    fun `unifying with unknowns should solve them`() {
        "u1" unifiedWith "Int"
        "u2" unifiedWith "String"
        "u3" unifiedWith "u4"
        "u5" unifiedWith "String -> String"

        1 shouldBeSolved "Int"
        2 shouldBeSolved "String"
        3 shouldBeSolved "u4"
        5 shouldBeSolved "String -> String"
    }

    @Test
    fun `unifying with unknowns is commutative`() {
        "Int" unifiedWith "u1"
        "String" unifiedWith "u2"
        "u4" unifiedWith "u3"
        "String -> String" unifiedWith "u5"

        1 shouldBeSolved "Int"
        2 shouldBeSolved "String"
        4 shouldBeSolved "u3"
        5 shouldBeSolved "String -> String"
    }

    @Test
    fun `unifying an unknown with itself shouldn't change the solution`() {
        "u1" unifiedWith "u1"
        1 shouldBeSolved "u1"

        assertEquals(typeChecker.checkState, CheckState())
    }

    @Test
    fun `unifying must not create circular solutions`() {
        "u1" unifiedWith "u2"
        "u2" unifiedWith "u1"

        // They could also both solve to u1. If that's the case for your implementation
        // just change this test around
        1 shouldBeSolved "u2"
        2 shouldBeSolved "u2"
    }

    @Test
    fun `unifying must perform the occurs check`() {
        assertThrows(Exception::class.java) {
            "u1" unifiedWith "u1 -> u1"
        }
        assertThrows(Exception::class.java) {
            "u1 -> u1" unifiedWith "u1"
        }
    }

    @Test
    fun `unify takes existing substitution into account`() {
        given(1 to "Int")

        "u1" unifiedWith "u5"

        5 shouldBeSolved "Int"
    }

    @Test
    fun `unify fails transitively`() {
        given(
            1 to "Int",
            2 to "u3",
            3 to "String"
        )

        failsWith(UnificationFailure("Int", "String")) { "u1" unifiedWith "u3" }
    }

    @Test
    fun `unify works on function types`() {
        "String -> Int" unifiedWith "String -> Int"
        "Bool -> Bool" unifiedWith "Bool -> Bool"
    }

    @Test
    fun `unify solves through function types`() {
        "u1 -> String" unifiedWith "Int -> u2"

        1 shouldBeSolved "Int"
        2 shouldBeSolved "String"

        "u3 -> Int" unifiedWith "(Bool -> String) -> Int"

        3 shouldBeSolved "Bool -> String"
    }

    @Test
    fun `unify shouldn't create circular solutions for function types either`() {
        "u1 -> u2" unifiedWith "u2 -> u1"

        // They could also both solve to u1. If that's the case for your implementation
        // just change this test around
        1 shouldBeSolved "u2"
        2 shouldBeSolved "u2"
    }














    // ================ Testing DSL from here ================

    private fun given(vararg substs: Pair<Int, String>) {
        substs.forEach {
            typeChecker.checkState.substitution.subst[it.first] = Parser.parseTestType(it.second)
        }
    }

    private infix fun String.unifiedWith(other: String) {
        val ty1 = Parser.parseTestType(this)
        val ty2 = Parser.parseTestType(other)
        typeChecker.unify(ty1, ty2)
    }

    private infix fun Int.shouldBeSolved(ty: String) {
        assertEquals(
            Parser.parseTestType(ty),
            typeChecker.zonk(Monotype.Unknown(this))
        )
    }

    private data class UnificationFailure(val ty1: String, val ty2: String)

    private fun failsWith(u: UnificationFailure, act: () -> Unit) {
        val exception: Exception = assertThrows(Exception::class.java, act)
        assertEquals(
            true,
            exception.message == "Can't match ${u.ty1} with ${u.ty2}" ||
                    exception.message == "Can't match ${u.ty2} with ${u.ty1}"
        ) {
            "$this\n  should have failed to unify\n${u.ty1} with ${u.ty2}\n  but instead failed with\n${exception.message}\n"
        }
    }
}



