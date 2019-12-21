package types

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import syntax.Monotype
import syntax.TyVar

@Tag("substitution")
internal class SubstitutionTest {

    val subst = Substitution(
        hashMapOf(
            1 to Monotype.Int,
            2 to Monotype.String,
            3 to Monotype.Unknown(1),
            4 to Monotype.Function(Monotype.Unknown(1), Monotype.Unknown(2))
        )
    )

    @Test
    fun `applying a substitution to a primitive type doesn't change it`() {
        assertEquals(subst.apply(Monotype.Int), Monotype.Int)
        assertEquals(subst.apply(Monotype.String), Monotype.String)
        assertEquals(subst.apply(Monotype.Bool), Monotype.Bool)
    }

    @Test
    fun `applying a substitution to an unknown replaces it with the type in the Substitution`() {
        assertEquals(subst.apply(Monotype.Unknown(1)), Monotype.Int)
        assertEquals(subst.apply(Monotype.Unknown(2)), Monotype.String)
    }

    @Test
    fun `applying a substitution to a function type applies it to both its argument and result type`() {
        assertEquals(
            subst.apply(Monotype.Function(Monotype.Unknown(1), Monotype.Unknown(2))),
            Monotype.Function(Monotype.Int, Monotype.String)
        )
    }

    @Test
    fun `applying a substitution works transitively (finds a fix-point)`() {
        assertEquals(
            subst.apply(Monotype.Unknown(3)),
            Monotype.Int
        )
    }

    @Test
    fun `putting it all together`() {
        assertEquals(
            subst.apply(Monotype.Unknown(4)),
            Monotype.Function(Monotype.Int, Monotype.String)
        )
    }
}