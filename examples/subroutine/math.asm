.text
soft_mul:
    add     s0, x0, x0      // initialize s0 to 0 for return value
    addi    a0, a0, -1      // decrement a0
    add     s0, s0, a1      // accumulate s0 by a1
    neq     a0, x0          // x1 != 0
    br      -3              // branch if true
    jalr    x0, 0(ra)       // unconditional jump back to return address

soft_divide:
    add     s0, x0, x0      // initialize s0 to 0 for return value
    add     t0, x0, x0      // initialize t0 to 0 for accumulator
    lt      a1, a0
    br      2
    jalr    x0, 0(ra)       // unconditional jump back to return address
    add     t0, t0, a1      // accumulate
    addi    s0, s0, 1       // subtractions
    lt      t0, a0
    br      -3
    addi    s0, s0, -1      // subtract 1 because we've added one too many
    jalr    x0, 0(ra)       // unconditional jump back to return address
