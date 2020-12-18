#include <cstdio>
#include <cstdint>
#include <vector>
#include <unistd.h>

#include "verilated.h"
#include "VCPU.h"

std::vector<uint8_t> defaultProgram = {
    0b00000000, // NOP          // NOP to flush pipeline
    0b01010110, // LDI #0x6     // Load 6 into A Register
    0b01000000, // STA $0x0     // Put 6 into 0x0 address
    0b01011111, // LDI #0xf     // Load 15 into A Register
    0b00100000, // ADD $0x0     // Add address STORE_A to reg A
    0b01000000, // STA $0x0     // Reg A into 0x0 address
    0b00010000, // LDA $0x0     // Load 0x0 into A Register
    0b00100000, // ADD $0x0     // Add address STORE_A to reg A
    0b11100000, // OUT          // Outputs contents of reg A
    0b11110000, // HLT          // Halt processor
};

void step(VCPU *cpu, int steps);

int main(int argc, char** argv, char** env) {
    Verilated::commandArgs(argc, argv);

    /* Attempt to load a binary file */
    std::vector<uint8_t> program;
    if (argc == 2)
    {
        FILE *f = fopen(argv[1], "rb");
        uint8_t byte;
        while (fread(&byte, 1, 1, f) > 0)
        {
            program.push_back(byte);
        }
        fclose(f);
    }
    else
    {
        /* Load the default program if there is no binary
         * file. */
        program = defaultProgram;
    }

    /* Initialize the top level CPU */
    VCPU* top = new VCPU;

    /* Prepare cpu for instruction load */
    top->io_halt = 0;
    top->io_load = 1;

    /* Load in program into instruction cache */
    int count = 0;
    for (auto ins : program) {
        top->io_addr = count++; /* Instruction address */
        top->io_data = ins; /* Instruction */

        /* Cycle the clock */
        step(top, 1);
    }
    top->io_load = 0;

    /* Run the program */
    while (!top->io_halt) {
        /* Cycle the clock */
        step(top, 1);

        /* Print CPU states */
        VL_PRINTF("Out[%i]: ins 0x%2X res %u \n",
            top->CPU__DOT__pc__DOT__register,
            top->CPU__DOT__insReg, /* Internal variable */
            top->io_output); /* IO */

        /* Output */
        if (top->io_valid)
        {
            VL_PRINTF("We got an output! out: %u\n", top->io_output);
        }
    }
    delete top;
    exit(0);
}

void step(VCPU *cpu, int steps)
{
    for (int i=0; i < steps; i++)
    {
        /* Simulate Rising Edge */
        cpu->clock = 1;
        cpu->eval();

        /* Simulate Falling Edge */
        cpu->clock = 0;
        cpu->eval();
    }
}
