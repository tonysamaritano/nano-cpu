#include <iostream>
#include <chrono>

#include <unistd.h>

int main(int argc, char *argv[])
{
    const int operations = 5e7;

    float x=1.0f, y=2.0f, z;

    auto start = std::chrono::steady_clock::now();
    for (int i=0; i<operations; i++)
    {
        z = x * y;
    }
    auto end = std::chrono::steady_clock::now();

    std::chrono::duration<double> elapsed_seconds = end-start;
    std::cout << "elapsed time: " << elapsed_seconds.count() << "s\n";
    std::cout << "FLOPS: "
        << static_cast<double>(operations)/elapsed_seconds.count()
        << std::endl;

    return 0;
}