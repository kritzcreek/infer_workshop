package syntax

import syntax.Token.*

sealed class Token {
    override fun toString(): String = this.javaClass.simpleName

    companion object {
        inline fun <reified T> get(token: Token): T? {
            return token as? T
        }
    }

    object LParen : Token()
    object RParen : Token()
    object LBrace : Token()
    object RBrace : Token()
    object LAngle : Token()
    object RAngle : Token()
    object Lam : Token()
    object Dot : Token()
    object Comma : Token()
    object Semicolon: Token()
    object Colon : Token()
    object DoubleColon : Token()
    object Equals: Token()
    object Arrow : Token()
    data class Ident(val ident: String) : Token()
    data class UpperIdent(val ident: String) : Token()
    data class IntToken(val int: Int) : Token()
    data class BoolToken(val bool: Boolean) : Token()
    data class StringToken(val string: String) : Token()
    object EOF : Token()
    object Let: Token()
    object Rec: Token()
    object In: Token()
    object Forall: Token()
    object If: Token()
    object Then: Token()
    object Else: Token()
}

data class Position(val line: Int, val column: Int) {
    fun shift(n: Int) = copy(column = column + n)
    override fun toString(): String {
        return "$line:$column"
    }
}

data class Span(val start: Position, val end: Position) {
    companion object {
        val DUMMY = Span(Position(-1, -1), Position(-1, -1))
    }

    override fun toString(): String {
        return "${this.start}-${this.end}"
    }
}
data class Spanned<out T>(val span: Span, val value: T) {
    // This is a bit dodgy... but any other option is too tedious
    override fun equals(other: Any?): Boolean {
        if (other is Spanned<*>) {
            return this.value == other.value
        }
        return false
    }

    override fun hashCode(): Int {
        return value?.hashCode() ?: 0
    }
}

class Lexer(input: String) : Iterator<Spanned<Token>> {
    var iterator = CharLocations(input.iterator())

    init {
        consumeWhitespace()
    }

    override fun hasNext(): Boolean {
        return iterator.hasNext()
    }

    override fun next(): Spanned<Token> {
        val start = iterator.position

        if (!iterator.hasNext()) {
            return Spanned(Span(start, start), EOF)
        }

        val (token, length) = when (val c = iterator.next()) {
            '(' -> LParen to 1
            ')' -> RParen to 1
            '{' -> LBrace to 1
            '}' -> RBrace to 1
            '<' -> LAngle to 1
            '>' -> RAngle to 1
            '\\' -> Lam to 1
            '.' -> Dot to 1
            ';' -> Semicolon to 1
            ',' -> Comma to 1
            ':' -> {
                if (iterator.peek() == ':') {
                    iterator.next();
                    DoubleColon to 2
                } else {
                    Colon to 1
                }
            }
            '=' -> Equals to 1
            '-' -> if (iterator.next() == '>') Arrow to 2 else {
                throw RuntimeException()
            }
            '"' -> stringLiteral()
            else -> {
                if (c.isJavaIdentifierStart()) ident(c)
                else if (c.isDigit()) intLiteral(c)
                else throw RuntimeException()
            }
        }.also {
            consumeWhitespace()
        }

        return Spanned(Span(start, start.shift(length)), token)
    }

    private fun stringLiteral(): Pair<StringToken, Int> {
        var buffer = ""
        while (iterator.hasNext() && iterator.peek() != '"') {
            buffer += iterator.next()
        }
        if(!iterator.hasNext()) throw Exception("Unclosed String literal")
        iterator.next()
        return StringToken(buffer.replace("\\n", "\n")) to buffer.length + 2
    }

    private fun intLiteral(startChar: Char): Pair<Token, Int> {
        var result: String = startChar.toString()
        while (iterator.hasNext() && iterator.peek().isDigit()) {
            result += iterator.next()
        }
        return IntToken(result.toInt()) to result.length
    }

    private fun consumeWhitespace() {
        while (iterator.hasNext() && iterator.peek().isWhitespace())
            iterator.next()
    }

    private fun ident(startChar: Char): Pair<Token, Int> {
        var result: String = startChar.toString()
        while (iterator.hasNext() && iterator.peek().isJavaIdentifierPart()) {
            result += iterator.next()
        }

        return when (result) {
            "true" -> BoolToken(true)
            "false" -> BoolToken(false)
            "let" -> Let
            "rec" -> Rec
            "in" -> In
            "if" -> If
            "then" -> Then
            "else" -> Else
            "forall" -> Forall
            else -> if (result.get(0).isUpperCase()) { UpperIdent(result) } else { Ident(result) }
        } to result.length
    }
}

class PeekableIterator<T>(private val iterator: Iterator<T>) : Iterator<T> {

    private var lookahead: T? = null

    override fun hasNext(): Boolean {
        return if (lookahead != null) {
            true
        } else {
            iterator.hasNext()
        }
    }

    override fun next(): T {
        return if (lookahead != null) {
            val temp = lookahead!!
            lookahead = null
            temp
        } else {
            iterator.next()
        }
    }

    fun peek(): T {
        lookahead = lookahead ?: iterator.next()
        return lookahead!!
    }
}

class CharLocations(private val iterator: Iterator<Char>) : Iterator<Char> {

    private var lookahead: Char? = null
    var position = Position(1, 0)

    override fun hasNext(): Boolean {
        return if (lookahead != null) {
            true
        } else {
            iterator.hasNext()
        }
    }

    override fun next(): Char {
        return if (lookahead != null) {
            val temp = lookahead!!
            lookahead = null
            nextChar(temp)
        } else
            nextChar(iterator.next())
    }

    fun peek(): Char {
        lookahead = lookahead ?: iterator.next()
        return lookahead!!
    }

    private fun nextChar(c: Char): Char {
        position = if (c == '\n') {
            Position(position.line + 1, 0)
        } else {
            Position(position.line, position.column + 1)
        }

        return c
    }
}
