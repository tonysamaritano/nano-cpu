TOP=VCPU
SRC=CPU.v
BIN_DIR=obj_dir

default: all

clean:
	rm -r ${BIN_DIR}

compile-verilog:
	mkdir -p ${BIN_DIR}
	sbt "runMain cpu.main --target-dir ${BIN_DIR}"

verilate:
	# Verilator creates cpp code out of verilog files
	verilator --cc ${BIN_DIR}/${SRC} --exe src/main/cpp/main.cpp --trace

	# Compiles generated cpp into library
	$(MAKE) -j -C ${BIN_DIR} -f ${TOP}.mk ${TOP}__ALL.a

build-cpu:
	# Compiles wrapper for simulator
	$(MAKE) -j -C ${BIN_DIR} -f ${TOP}.mk main.o verilated.o verilated_vcd_c.o

	# Builds Simulation Application
	g++ ${BIN_DIR}/main.o \
		${BIN_DIR}/${TOP}__ALL.a \
		${BIN_DIR}/verilated.o \
		${BIN_DIR}/verilated_vcd_c.o \
		-o ${BIN_DIR}/${TOP}

all: compile-verilog verilate build-cpu