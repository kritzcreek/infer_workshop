package syntax

import pretty.*

fun Expression.show(): Doc<Nothing> = showInner(0)
fun Expression.pretty(): kotlin.String {
    return show().pretty(60, 0.4F)
}

fun Expression.showInner(depth: kotlin.Int): Doc<Nothing> = when (this) {
    is Expression.Int -> int.toString().text()
    is Expression.Bool -> bool.toString().text()
    is Expression.String -> string.toString().text()
    is Expression.Var -> name.show()
    is Expression.Lambda -> {
        var res = (("\\".text<Nothing>() + binder.show() + dot()).group() + space() + body.show()).group().nest(2)
        if (depth > 0) res = res.enclose(lParen(), rParen())
        res
    }
    is Expression.App -> {
        val (func, args) = unfoldApps()
        var res = ((listOf(func) + args).map { it.showInner(1) }).sep().group().nest(2)
        if (depth > 0) res = res.enclose(lParen(), rParen())
        res
    }
    is Expression.Let ->
        "let".text<Nothing>() + space() + binder.show() + space() + equals() + space() +
                expr.show() + space() + "in".text() + line() +
                body.show()
    is Expression.If -> (("if".text<Nothing>() + space() + condition.show() + space() + "then".text()).group() +
            space() + thenCase.show()).group().nest(2) +
            space() + ("else".text<Nothing>() + space() + elseCase.show()).group().nest(2)
}

fun Expression.App.unfoldApps(): Pair<Expression, List<Expression>> {
    return if (function is Expression.App) {
        val (func, args) = function.unfoldApps()
        func to args + listOf(argument)
    } else {
        function to listOf(argument)
    }
}

private fun Monotype.prettyInner(nested: Boolean): kotlin.String {
    return when (this) {
        is Monotype.Unknown -> "u${this.u}"
        is Monotype.Function -> {
            val res = "${this.argument.prettyInner(true)} -> ${this.result.pretty()}"
            if(nested) "($res)" else res
        }
        Monotype.Int -> "Int"
        Monotype.String -> "String"
        Monotype.Bool -> "Bool"
    }
}

fun Monotype.pretty(): kotlin.String = prettyInner(false)
fun Name.show(): Doc<Nothing> = v.text()