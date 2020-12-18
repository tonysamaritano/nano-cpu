NOP          // NOP to flush pipeline
LDI 0x6      // Load 6 into A Register
STA 0x0      // Put 6 into 0x0 address
LDI 0xf      // Load 15 into A Register
ADD 0x0      // Add address STORE_A to reg A
STA 0x0      // Reg A into 0x0 address
LDA 0x0      // Load 0x0 into A Register
ADD 0x0      // Add address STORE_A to reg A
OUT          // Outputs contents of reg A
HLT          // Halt processor