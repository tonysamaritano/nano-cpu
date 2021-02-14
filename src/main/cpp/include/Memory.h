#pragma once
#include <vector>
#include <cstdint>
#include <algorithm>

template<typename U>
class Memory
{
public:
    Memory() = delete;
    Memory(unsigned int size)
    {
        m_memory.resize(size, 0);
    }

    U &operator[](int addr)
    {
        return *reinterpret_cast<U*>(&m_memory[addr]);
    }


private:
    std::vector<uint8_t> m_memory;
};