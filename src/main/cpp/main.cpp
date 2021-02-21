#include <cstdio>
#include <cstdint>
#include <csignal>
#include <vector>
#include <unistd.h>
#include <cassert>
#include <iostream>
#include <iomanip>

#include <string>

#include "VCFU.h"

#include "Module.h"
#include "Memory.h"

static std::vector<uint16_t> program0 = {
    0b0010111000001000,
    0b1010010001001001,
    0b0010111000010000,
    0b1010001010010001,
    0b0000100001011000,
    0b0010110000000011,
    0b1110110000000111,
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
    0b1110110000000111,
};

class Processor : public Module<VCFU>
{
public:
    Processor(std::string tracefile, int verbosity = 0) :
        Module(tracefile),
        m_verbosity(verbosity) {}

    void step() final
    {
        if ((io_mem_write_we && m_verbosity > 0) || m_verbosity > 1) {
            printf("R: Mem[0x%02X] 0x%04X\tW: En: %s mem[%u]: %u\n",
                io_mem_read_addr,
                io_mem_read_data,
                io_mem_write_we ? "Yes" : " No",
                io_mem_write_addr,
                io_mem_write_data);
        }

        Module<VCFU>::step();
    }

private:
    int m_verbosity = 0;
};

void help();

int main(int argc, char *argv[]) {
    Verilated::commandArgs(argc, argv);

    char c;
    int verbosity = 0;
    std::string bin, tracefile;
    while ((c = getopt (argc, argv, "vb:t:h")) != -1)
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
        case 'h':
            help();
            return 0;
        default:
            break;
        }
    }

    /* Initialize the top level CPU */
    Processor top(tracefile, verbosity);

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

    while (!top.io_halt)
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

void help()
{
    using namespace std;
    int argwidth = 14;
    int descwidth = 40;
    cout << "usage: VProcessor [options]" << endl << endl;
    cout << "  " << setw(argwidth) << left <<   "-h"
        <<  setw(descwidth) << left <<          "help text" << endl;
    cout << "  " << setw(argwidth) << left <<   "-v"
        <<  setw(descwidth) << left <<          "verbosity level e.g. -v = level 1 -vv = level 2..." << endl;
    cout << "  " << setw(argwidth) << left <<   "-b <file>"
        <<  setw(descwidth) << left <<          "binary program file" << endl;
    cout << "  " << setw(argwidth) << left <<   "-t <file>"
        <<  setw(descwidth) << left <<          "output trace file (.vcd)" << endl;
}
