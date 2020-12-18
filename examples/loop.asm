NOP
LDI 0x1 // Load 1 into reg A
ADD 0x0 // Add contents of 0x00 and reg A
STA 0x0 // Store contents of reg A into 0x00
OUT     // Output reg A
JMP 0x1 // Jump to the top
