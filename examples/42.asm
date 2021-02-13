addi x1, x0, 7      // load 7 into x1
slli x1, x1, 2      // shift left by 2 to multiply x1 by 4
addi x2, x0, 7      // load 7 into x2
slli x2, x2, 1      // shift left by 1 to multiply x2 by 2
add  x3, x1, x2     // add x1 and x2 into x3
sw   x0, x3, 0      // store x3 in 0(x0)
hlt  x0, x0, x0     // Halt