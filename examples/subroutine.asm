.data

msg:
    .string "Hello!\n"

life:
    .word 42

balls:
    .word 12345

.text
    global _start

soft_mul:
    add     s0, x0, x0      // initialize s0 to 0 for return value
    addi    a0, a0, -1      // decrement a0
    add     s0, s0, a1      // accumulate s0 by a1
    neq     a0, x0          // x1 != 0
    br      -3              // branch if true
    jalr    x0, 0(ra)       // unconditional jump back to return address

output_42:
    lw      t0, life        // load data 'life' from memory
    sw      x0, 0(t0)       // store life to 0
    jalr    x0, 0(ra)       // unconditional jump back to return address

store_far:
    lli     255             // loads 0xff into lower byte
    luai    t0, 128         // load 0x80 into up to create 0x80ff

    // Stores 'balls' location in a1
    lli     balls
    luai    t1, 0
    lw      a1, 0(t1)

    sw      t0, 0(a1)
    jalr    x0, 0(ra)      // unconditional jump back to return address

_start:
    lw      a1, life       // load data 'life' from memory
    addi    a0, x0, 7      // set a0 to 7
    jal     ra, soft_mul   // jump to soft_mul and store return address in ra
    sw      x0, 0(s0)      // store the answer from s0 to answer
    jal     ra, output_42  // jump to output_42 and store return address in ra
    jal     ra, store_far  // jump to store_far and store return address in ra
    hlt     x0, x0, x0




