#include "VCPU.h"
#include "verilated.h"
#include <unistd.h>

int main(int argc, char** argv, char** env) {
    Verilated::commandArgs(argc, argv);

    /* Initialize the top level CPU */
    VCPU* top = new VCPU;
    top->io_halt = 0;

    int count = 0;
    while (!Verilated::gotFinish() && count < 12) {
        /* Simulate Rising Edge */
        top->clock = 1;
        top->eval();

        /* Simulate Falling Edge */
        top->clock = 0;
        top->eval();

        // usleep(5e5);
        /* Print CPU states */
        VL_PRINTF("Out[%i]: ins 0x%2X res %u \n",
            count++,
            top->CPU__DOT__fetch__DOT__insReg, /* Internal variable */
            top->io_res); /* IO */
    }
    delete top;
    exit(0);
}