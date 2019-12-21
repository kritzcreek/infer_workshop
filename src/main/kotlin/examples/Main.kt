package examples

import interpret.runProg

fun main() {
    runProg("Just an Int", primInt)
    runProg("Just a Bool", primBool)
    runProg("Just a String", primString)
    runProg("Just a Var", simpleVar)
    runProg("Simple let", simpleLet)
    runProg("Simple Lambda", simpleLambda)
    runProg("Simple Application", simpleApplication)
    runProg("Huge if true", hugeIfTrue)
    runProg("Ill-typed if", illtypedIf)
    runProg("Let & Application", letAndApply)
    runProg("Currying 1", currying1)
    runProg("Currying 2", currying2)
    runProg("Higher order programming", higherOrder)
    runProg("Fibonacci", fib)
    runProg("Ninety-nine Bottles", ninetyninebottles)
}

val primInt = "1"

val primBool = "true"

val primString = """
    "Hello world!"
"""

val simpleVar = "x"

val simpleLet = """
    let x = 10 in
    x
"""

val simpleLambda = """
    \x -> x
"""

val simpleApplication = """
    (\x -> x) "Hello"
"""

val hugeIfTrue = """
    if true then
      "HUGE"
    else 
      "smol"
"""

val illtypedIf = """
    if false then
      "HUGE"
    else 
      20
"""

val letAndApply = """
    let identity = \x -> x in
    identity "Hello friends"
"""

val currying1 = """
    let const = \x -> \y -> x in
    const 10
"""

val currying2 = """
    let const = \x -> \y -> x in
    const 10 "Hello friends"
"""

val higherOrder = """
    let flip = \f -> \x -> \y -> f y x in
    let const = \x -> \y -> x in
    flip const 10 "Hello friends"
"""

val fib = """
let fib = \x -> 
    if int_equals x 1 then 
        1
    else if int_equals x 2 then
        1
    else
        add (fib (sub x 1)) (fib (sub x 2)) in
    fib 15
"""

val ninetyninebottles = """
let getabeer = \x ->
    if int_equals x 0 then
        "Go to the store and buy some more,\n99 bottles of beer on the wall.\n"
    else if int_equals x 1 then
        concat "1 bottle of beer on the wall,\n1 bottle of beer,\nTake one down, pass it around,\n0 bottle of beer on the wall.\n\n" (getabeer 0)
    else
        let init = concat (int_to_string x) " bottles of beer on the wall,\n" in
        let middle = concat (int_to_string x) " bottles of beer,\nTake one down, pass it around,\n" in
        let end = concat (int_to_string (sub x 1)) " bottles of beer on the wall.\n\n" in
        concat init (concat middle (concat end (getabeer (sub x 1)))) 
in
getabeer 99
"""
