package types

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentHashMapOf
import syntax.*
import kotlin.Exception

inline class Substitution(val subst: HashMap<Int, Monotype> = hashMapOf()) {
    fun apply(ty: Monotype): Monotype {
        // TODO
        return ty
    }

    operator fun set(u: Int, ty: Monotype) {
        subst[u] = ty
    }

    override fun toString(): String =
        "{ " + subst.toList().joinToString("\n, ") { (u, ty) -> "u$u ↦ ${ty.pretty()}" } + "\n}"
}

inline class Environment(val env: PersistentMap<Name, Monotype> = persistentHashMapOf()) {
    operator fun get(name: Name): Monotype? = env[name]
    fun extend(name: Name, ty: Monotype) = env.put(name, ty)
}

class TypeChecker {
    var freshSupply: Int = 0
    var substitution: Substitution = Substitution()

    // Returns a fresh `Unknown`, where fresh means "not ever used before"
    private fun freshUnknown(): Monotype = Monotype.Unknown(++freshSupply)

    // Applies the current substitution to a given type
    fun zonk(ty: Monotype): Monotype = substitution.apply(ty)

    fun unify(ty1: Monotype, ty2: Monotype) {
        val ty1 = zonk(ty1)
        val ty2 = zonk(ty2)
        throw Exception("Can't match ${ty1.pretty()} with ${ty2.pretty()}")
    }

    private fun infer(env: Environment, expr: Expression): Monotype {
        TODO()
    }

    fun inferExpr(env: Environment, expr: Expression): Monotype = zonk(infer(env, expr))
}