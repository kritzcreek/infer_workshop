package interpret

import kotlinx.collections.immutable.persistentHashMapOf
import syntax.*
import types.TypeChecker

inline class Environment(val env: HashMap<Name, IR> = HashMap()) {
    operator fun get(name: Name): IR? = env[name]
    fun copy(): Environment = Environment(HashMap(env))
    operator fun set(binder: Name, value: IR) {
        env[binder] = value
    }

    override fun toString(): String = "<env>"
}

sealed class IR {
    data class Int(val int: kotlin.Int) : IR()
    data class Bool(val bool: Boolean) : IR()
    data class String(val string: kotlin.String) : IR()
    data class Var(val name: Name) : IR()
    data class Lambda(val binder: Name, val body: IR) : IR()
    data class Closure(val binder: Name, val body: IR, val env: Environment) : IR()
    data class App(val function: IR, val argument: IR) : IR()
    data class Let(val recursive: Boolean, val binder: Name, val expr: IR, val body: IR) : IR()
    data class If(val condition: IR, val thenCase: IR, val elseCase: IR) : IR()

    fun eval(env: Environment): IR =
        when (this) {
            is Int, is Bool, is String, is Closure -> this
            is Var -> if (name.v.startsWith("#")) {
                evalPrim(env, name.v)
            } else {
                env[name] ?: throw Exception("Unknown variable $name")
            }
            is Lambda -> Closure(binder, body, env)
            is App -> {
                when (val closure = function.eval(env)) {
                    is Closure -> {
                        val tmpEnv = closure.env.copy()
                        tmpEnv[closure.binder] = argument.eval(env)
                        closure.body.eval(tmpEnv)
                    }
                    else -> throw Exception("$closure is not a function")
                }
            }
            is Let -> {
                if (recursive) {
                    val tmpEnv = env.copy()
                    val closure = expr.eval(env)
                    if (closure !is Closure) throw Exception("Only functions may be declared recursively")
                    closure.env[binder] = closure
                    tmpEnv[binder] = closure
                    body.eval(tmpEnv)
                } else {
                    val tmpEnv = env.copy()
                    tmpEnv[binder] = expr.eval(env)
                    body.eval(tmpEnv)
                }
            }
            is If -> {
                if (condition.eval(env).matchBool()) {
                    thenCase.eval(env)
                } else {
                    elseCase.eval(env)
                }
            }
        }

    fun matchInt(): kotlin.Int =
        (this as? Int)?.int ?: throw Exception("Expected an Int but got $this")

    fun matchBool(): Boolean =
        (this as? Bool)?.bool ?: throw Exception("Expected a Bool but got $this")

    fun matchString(): kotlin.String =
        (this as? String)?.string ?: throw Exception("Expected a String but got $this")


    companion object {
        val initialEnv: Environment = Environment(
            hashMapOf(
                primBinary("add"),
                primBinary("sub"),
                primBinary("int_equals"),
                primBinary("concat"),
                primUnary("int_to_string")
            )
        )

        private fun primUnary(prim: kotlin.String): Pair<Name, IR> =
            Name(prim) to Closure(Name("x"), Var(Name("#$prim")), Environment())

        private fun primBinary(prim: kotlin.String): Pair<Name, IR> =
            Name(prim) to Closure(Name("x"), Lambda(Name("y"), Var(Name("#$prim"))), Environment())

        fun fromExpr(expr: Expression): IR = when (expr) {
            is Expression.Int -> Int(expr.int)
            is Expression.Bool -> Bool(expr.bool)
            is Expression.String -> String(expr.string)
            is Expression.Var -> Var(expr.name)
            is Expression.Lambda -> Lambda(expr.binder, fromExpr(expr.body))
            is Expression.App -> App(fromExpr(expr.function), fromExpr(expr.argument))
            is Expression.Let ->
                Let(expr.freeVars().contains(expr.binder), expr.binder, fromExpr(expr.expr), fromExpr(expr.body))
            is Expression.If -> If(fromExpr(expr.condition), fromExpr(expr.thenCase), fromExpr(expr.elseCase))
        }

        fun evalPrim(env: Environment, prim: kotlin.String): IR {
            return when (prim) {
                "#add" -> Int(env[Name("x")]!!.matchInt() + env[Name("y")]!!.matchInt())
                "#sub" -> Int(env[Name("x")]!!.matchInt() - env[Name("y")]!!.matchInt())
                "#int_equals" -> Bool(env[Name("x")]!!.matchInt() == env[Name("y")]!!.matchInt())
                "#concat" -> String(env[Name("x")]!!.matchString() + env[Name("y")]!!.matchString())
                "#int_to_string" -> String(env[Name("x")]!!.matchInt().toString())
                else -> throw Exception("Unknown primitive $prim")
            }
        }
    }
}

private fun Expression.freeVars(): HashSet<Name> =
    when (this) {
        is Expression.Int, is Expression.Bool, is Expression.String -> hashSetOf()
        is Expression.Var -> hashSetOf(name)
        is Expression.Lambda -> body.freeVars().also { it.remove(binder) }
        is Expression.App -> function.freeVars().also {
            it.addAll(argument.freeVars())
        }
        is Expression.Let -> body.freeVars().also {
            it.remove(binder)
            it.addAll(expr.freeVars())
        }
        is Expression.If -> condition.freeVars().also {
            it.addAll(thenCase.freeVars())
            it.addAll(elseCase.freeVars())
        }
    }

fun runProg(name: String, input: String) {
    println("\nRunning \"$name\":")

    val expr = Parser.parseExpression(input)
    try {
        val env = persistentHashMapOf(*listOf(
            "add" to "Int -> Int -> Int",
            "sub" to "Int -> Int -> Int",
            "int_equals" to "Int -> Int -> Bool",
            "concat" to "String -> String -> String",
            "int_to_string" to "Int -> String"
        ).map { (name, ty) ->
            Name(name) to Parser.parseTestType(ty)
        }.toTypedArray()
        )

        val ty = TypeChecker().inferExpr(types.Environment(env), expr)
        println("Inferred ${ty.pretty()}")
    } catch (ex: Exception) {
        println("Inference failed with: ${ex.message}")
    } catch (ex: NotImplementedError) {
        println("Inference failed with: ${ex.message}")
    }
    val ir = IR.fromExpr(expr)
    try {
        println(ir.eval(IR.initialEnv))
    } catch (ex: Exception) {
        println("Execution failed with: ${ex.message}")
    }
}