package types

import syntax.*
import kotlin.Exception

inline class Substitution(val subst: HashMap<Int, Monotype> = hashMapOf()) {
    fun apply(ty: Monotype): Monotype {
        // TODO
        return ty
    }

    override fun toString(): String =
        "{ " + subst.toList().joinToString("\n, ") { (u, ty) -> "u$u ↦ ${ty.pretty()}" } + "\n}"
}

inline class Environment(val env: HashMap<Name, Monotype> = hashMapOf()) {
    fun clone(): Environment = Environment(HashMap(env))
    operator fun get(name: Name): Monotype? = env[name]
    operator fun set(name: Name, ty: Monotype) {
        env[name] = ty
    }
}

data class CheckState(
    var environment: Environment = Environment(),
    val substitution: Substitution = Substitution(),
    var fresh_supply: Int = 0
)

class TypeChecker(var checkState: CheckState) {

    // Returns a fresh `Unknown`, where fresh means "not ever used before"
    private fun freshUnknown(): Monotype = Monotype.Unknown(++checkState.fresh_supply)

    // Applies the current substitution to a given type
    fun zonk(ty: Monotype): Monotype = checkState.substitution.apply(ty)

    fun unify(ty1: Monotype, ty2: Monotype) {
        val ty1 = zonk(ty1)
        val ty2 = zonk(ty2)
        throw Exception("Can't match ${ty1.pretty()} with ${ty2.pretty()}")
    }

    private fun infer(expr: Expression): Monotype {
        TODO()
    }

    fun inferExpr(expr: Expression): Monotype = zonk(infer(expr))
}