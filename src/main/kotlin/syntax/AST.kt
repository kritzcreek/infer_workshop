package syntax

inline class Name(val v: String) {
    override fun toString(): String = v
}

sealed class Expression {
    data class Int(val int: kotlin.Int) : Expression()
    data class Bool(val bool: Boolean) : Expression()
    data class String(val string: kotlin.String) : Expression()
    data class Var(val name: Name) : Expression()
    data class Let(val binder: Name, val expr: Expression, val body: Expression) : Expression()
    data class Lambda(val binder: Name, val body: Expression) : Expression()
    data class App(val function: Expression, val argument: Expression) : Expression()
    data class If(val condition: Expression, val thenCase: Expression, val elseCase: Expression) : Expression()
}

sealed class Monotype {
    object Int : Monotype()
    object String : Monotype()
    object Bool : Monotype()
    data class Function(val argument: Monotype, val result: Monotype) : Monotype()
    data class Unknown(val u: kotlin.Int) : Monotype()

    fun over(f: (Monotype) -> Monotype): Monotype =
        when (this) {
            Int, String, Bool, is Unknown -> f(this)
            is Function -> f(Function(argument.over(f), result.over(f)))
        }

    fun unknowns(): HashSet<kotlin.Int> {
        val res = hashSetOf<kotlin.Int>()
        over {
            if (it is Unknown) it.also { res.add(it.u) }
            else it
        }
        return res
    }

    override fun toString() = pretty()
}

inline class TyVar(val v: String) {
    override fun toString(): String = v
}