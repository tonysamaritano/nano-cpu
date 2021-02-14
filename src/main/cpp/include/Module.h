#pragma once
#include <string>
#include <cstdio>

#include "verilated.h"
#include "verilated_vcd_c.h"

template<class V>
class Module : public V
{
public:
    Module(std::string tracefile = "")
    {
        if (!tracefile.empty())
        {
            Verilated::traceEverOn(true);
            m_trace = new VerilatedVcdC;
            this->trace(m_trace, 99); // Trace 99 levels of hierarchy
            m_trace->open(tracefile.c_str());
        }
    }

    ~Module()
    {
        this->final();

        if (m_trace)
        {
            m_trace->close();
            delete m_trace;
        }
    }

    virtual void step()
    {
        this->eval();
        if (m_trace) m_trace->dump(m_time++);

        /* Simulate Rising Edge */
        this->clock = 1;
        this->eval();

        if (m_trace) m_trace->dump(m_time++);

        /* Simulate Falling Edge */
        this->clock = 0;
        this->eval();
    }
private:
    VerilatedVcdC *m_trace = nullptr;
    unsigned int m_time = 0;
};