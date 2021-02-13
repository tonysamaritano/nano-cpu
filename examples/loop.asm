addi x1, x0, 7      // puts 7 in x1
addi x2, x0, 6      // puts 6 in x2
addi x3, x0, 0      // puts 0 in x3
slli x2, x2, 5      // multiply x2 by 32
addi x2, x2, -1     // subtract x2 by -1
add  x3, x3, x2     // accumulate x3 by x2
addi x1, x1, -1     // decrement x1
neq  x1, x0         // x1 != 0
br   -3             // branch if true
sw   x0, x3, 0      // store x3 in 0(x0)
