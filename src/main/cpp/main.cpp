#include <cstdio>
#include <cstdint>
#include <csignal>
#include <vector>
#include <unistd.h>
#include <cassert>

#include <string>

#include "VProcessor.h"

#include "Module.h"
#include "Memory.h"

static std::vector<uint16_t> program0 = {
    0b0010111000001000,
    0b1010010001001001,
    0b0010111000010000,
    0b1010001010010001,
    0b0000100001011000,
    0b0010110000000011,
};

static std::vector<uint16_t> program1 = {
    0b0010111000001000,
    0b0010110000010000,
    0b1010101010010001,
    0b0000000000011000,
    0b0011111010010000,
    0b0000110010011000,
    0b0011111001001000,
    0b0010000001000100,
    0b1001101111111011,
    0b0010110000000011,
};

class Processor : public Module<VProcessor>
{
public:
    Processor(std::string tracefile) : Module(tracefile) {}

    void step() final
    {
        if(io_mem_write_we || true){
            printf("R: Mem[%2u] 0x%04X\tW: En: %s mem[%u]: %u\n",
                io_mem_read_addr,
                io_mem_read_data,
                io_mem_write_we ? "Yes" : " No",
                io_mem_write_addr,
                io_mem_write_data);
        }

        Module<VProcessor>::step();
    }
};

int main(int argc, char *argv[]) {
    Verilated::commandArgs(argc, argv);

    char c;
    int verbosity = 0;
    std::string bin, tracefile = "test.gtk";
    while ((c = getopt (argc, argv, "vb:t:")) != -1)
    {
        switch (c)
        {
        case 'v':
            verbosity++;
            break;
        case 'b':
            bin = std::string(optarg);
            break;
        case 't':
            tracefile = std::string(optarg);
            break;
        default:
            break;
        }
    }

    /* Initialize the top level CPU */
    Processor top(tracefile);

    /* Initialize Memory */
    Memory<uint16_t> memory(1024);

    /* Initialize Program */
    std::vector<uint16_t> program;
    if (bin.empty())
    {
        /* Load the default program if there is no binary
         * file. */
        program = program0;
    }
    else
    {
        FILE *f = fopen(bin.c_str(), "rb");
        uint16_t ins;
        while (fread(&ins, 1, sizeof(uint16_t), f) > 0)
        {
            program.push_back(ins);
        }
        fclose(f);
    }

    int i = 0;
    for (auto ins : program)
    {
        memory[i] = ins;
        i = i + 2;
    }

    for (int i=0; i<program.size()+30; i++)
    {
        top.io_mem_read_data = memory[top.io_mem_read_addr];
        top.step();
        if (top.io_mem_write_we)
        {
            memory[top.io_mem_write_addr] = top.io_mem_write_data;
        }
    }

    return 0;
}