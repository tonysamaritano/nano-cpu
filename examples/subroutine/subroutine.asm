.data

msg:
    .string "Hello\n"

life:
    .word 42

balls:
    .word 12345

.text
    entry _start

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

divide_test:
    // Load 64 into a0
    lli     64
    luai    a0, 0

    addi    a1, x0, 6       // Load 6 into a1
    add     sp, x0, ra      // store return address in stack pointer
    jal     ra, soft_divide // 64/6=10
    add     ra, x0, sp      // restore return address
    sw      x0, 0(s0)       // Store result at 0
    jalr    x0, 0(ra)       // unconditional jump back to return address

_start:
    lw      a1, life         // load data 'life' from memory
    addi    a0, x0, 7        // set a0 to 7
    jal     ra, soft_mul     // jump to soft_mul and store return address in ra
    sw      x0, 0(s0)        // store the answer from s0 to answer
    jal     ra, output_42    // jump to output_42 and store return address in ra
    jal     ra, store_far    // jump to store_far and store return address in ra
    jal     ra, divide_test  // jump to divide_test and store return address in ra
    hlt     x0, x0, x0




